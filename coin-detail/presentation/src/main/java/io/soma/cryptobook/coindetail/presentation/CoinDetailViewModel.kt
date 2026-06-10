package io.soma.cryptobook.coindetail.presentation

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.helpingstar.kandle.data.Candle
import io.github.helpingstar.kandle.data.FinancialChartModelProducer
import io.soma.cryptobook.coindetail.domain.model.CoinCandleVO
import io.soma.cryptobook.coindetail.domain.usecase.ObserveCoinDetailUseCase
import io.soma.cryptobook.coindetail.domain.usecase.ObserveIsWatchlistedUseCase
import io.soma.cryptobook.coindetail.domain.usecase.ToggleWatchlistUseCase
import io.soma.cryptobook.coindetail.presentation.CoinDetailContract.Effect
import io.soma.cryptobook.coindetail.presentation.CoinDetailContract.Event
import io.soma.cryptobook.coindetail.presentation.CoinDetailContract.State
import io.soma.cryptobook.coindetail.presentation.CoinDetailContract.ViewModel
import io.soma.cryptobook.coindetail.presentation.mapper.CoinDetailPresentationModelMapper
import io.soma.cryptobook.coindetail.presentation.mapper.toChartSeries
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.util.Text
import io.soma.cryptobook.core.designsystem.util.asText
import io.soma.cryptobook.core.domain.image.CoinImageResolver
import io.soma.cryptobook.core.domain.usecase.MarketRealtimeState
import io.soma.cryptobook.core.domain.usecase.ObserveMarketRealtimeState
import io.soma.cryptobook.core.presentation.message.MessageHelper
import io.soma.cryptobook.core.presentation.mvi.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CoinDetailViewModel.Factory::class)
class CoinDetailViewModel @AssistedInject constructor(
    @Assisted private val coinName: String,
    private val observeCoinDetailUseCase: ObserveCoinDetailUseCase,
    private val observeIsWatchlistedUseCase: ObserveIsWatchlistedUseCase,
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase,
    private val mapper: CoinDetailPresentationModelMapper,
    private val coinImageResolver: CoinImageResolver,
    private val messageHelper: MessageHelper,
    private val observeMarketRealtimeState: ObserveMarketRealtimeState,
) : BaseViewModel<State, Event, Effect>(
    State(
        symbol = coinName,
        imageUrl = coinImageResolver.getImageUrl(coinName),
    ),
),
    ViewModel {
    override val chartProducer = FinancialChartModelProducer()

    private var observeJob: Job? = null
    private var lastChartCandles: List<Candle> = emptyList()

    @AssistedFactory
    interface Factory {
        fun create(coinName: String): CoinDetailViewModel
    }

    init {
        ensureObserving()
        observeRealtimeState()
        observeWatchlisted()
    }

    override fun event(event: Event) {
        when (event) {
            Event.OnBackClicked -> emitEffect(Effect.NavigateBack)
            Event.OnScreenStarted -> ensureObserving()
            Event.OnFavoriteClicked -> viewModelScope.launch { toggleWatchlistUseCase(coinName) }
        }
    }

    private fun observeWatchlisted() {
        viewModelScope.launch {
            observeIsWatchlistedUseCase(coinName).collect { isWatchlisted ->
                updateState { state -> state.copy(isWatchlisted = isWatchlisted) }
            }
        }
    }

    private fun ensureObserving() {
        if (observeJob?.isActive == true) return

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observeCoinDetailUseCase(symbol = coinName).collect { result ->
                when (result) {
                    is ObserveCoinDetailUseCase.Result.Loading -> {
                        updateState { state ->
                            state.copy(isLoading = true, errorMsg = null)
                        }
                    }

                    is ObserveCoinDetailUseCase.Result.Success -> {
                        feedChart(result.candles)
                        updateState { state ->
                            mapper.toUiState(
                                vo = result.coinDetail,
                                orderBook = result.orderBook,
                                imageUrl = state.imageUrl,
                                isLoading = false,
                                errorMsg = null,
                            ).copy(isWatchlisted = state.isWatchlisted)
                        }
                    }

                    is ObserveCoinDetailUseCase.Result.Error.Connection -> {
                        updateState { state ->
                            state.copy(
                                isLoading = false,
                                errorMsg = CryptoString
                                    .crypto_coin_detail_connection_error_state
                                    .asText(),
                            )
                        }
                        messageHelper.showToast(
                            CryptoString.crypto_coin_detail_connection_error_toast.asText(),
                        )
                    }
                }
            }
        }
    }

    /**
     * Feeds the candle stream into [chartProducer] (vico/Kandle best practice, §Q13). The data layer
     * re-emits the full list on every tick, so this diffs the tail: a forming-bar update or single
     * append goes through the O(1) incremental path; a backfill/replace re-seeds the whole series.
     */
    private suspend fun feedChart(rawCandles: List<CoinCandleVO>) {
        val series = rawCandles.toChartSeries()
        val chartCandles = series.candles
        val chartVolumes = series.volumes
        if (chartCandles.isEmpty()) return

        if (isFullReload(lastChartCandles, chartCandles)) {
            chartProducer.runTransaction {
                price { candles(chartCandles) }
                volume(chartVolumes)
            }
        } else {
            if (chartCandles.size == lastChartCandles.size + 1) {
                // A new bar opened: finalize the previously-forming bar before appending the new one.
                val finalized = chartCandles.size - 2
                chartProducer.updateCandle(chartCandles[finalized])
                chartProducer.updateVolume(chartVolumes[finalized])
            }
            chartProducer.updateCandle(chartCandles.last())
            chartProducer.updateVolume(chartVolumes.last())
        }
        lastChartCandles = chartCandles
    }

    /**
     * True when [current] can't be expressed as a tail update on [previous] — a backfill/replace
     * rather than a forming-bar tick or single append. O(1): only head/tail timestamps are compared
     * (the historical prefix is stable between ticks).
     */
    private fun isFullReload(previous: List<Candle>, current: List<Candle>): Boolean {
        if (previous.isEmpty()) return true
        if (current.first().timestamp != previous.first().timestamp) return true
        return when (current.size) {
            previous.size -> current.last().timestamp != previous.last().timestamp
            previous.size + 1 -> current[previous.size - 1].timestamp != previous.last().timestamp
            else -> true
        }
    }

    private fun observeRealtimeState() {
        viewModelScope.launch {
            observeMarketRealtimeState().collect { runtimeState ->
                updateState { state ->
                    state.copy(realtimeStatusMessage = runtimeState.toRealtimeStatusText())
                }
            }
        }
    }

    private fun MarketRealtimeState.toRealtimeStatusText(): Text? = when (this) {
        MarketRealtimeState.Connected,
        MarketRealtimeState.Connecting,
        MarketRealtimeState.Inactive,
        -> null

        MarketRealtimeState.Recovering -> CryptoString.crypto_realtime_recovering.asText()

        is MarketRealtimeState.Failed -> CryptoString.crypto_realtime_disconnected.asText()
    }
}
