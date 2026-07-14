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
import io.soma.cryptobook.coindetail.presentation.component.ORDER_BOOK_ROW_COUNT
import io.soma.cryptobook.coindetail.presentation.component.OrderBookRowUiModel
import io.soma.cryptobook.coindetail.presentation.mapper.CoinDetailPresentationModelMapper
import io.soma.cryptobook.coindetail.presentation.mapper.toChartSeries
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.util.Text
import io.soma.cryptobook.core.designsystem.util.asText
import io.soma.cryptobook.core.domain.image.CoinImageResolver
import io.soma.cryptobook.core.domain.usecase.MarketRealtimeState
import io.soma.cryptobook.core.domain.usecase.ObserveMarketRealtimeState
import io.soma.cryptobook.core.ui.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

/**
 * ViewModel for the Coin Detail screen.
 */
@HiltViewModel(assistedFactory = CoinDetailViewModel.Factory::class)
class CoinDetailViewModel @AssistedInject constructor(
    @Assisted private val coinName: String,
    observeMarketRealtimeState: ObserveMarketRealtimeState,
    observeIsWatchlistedUseCase: ObserveIsWatchlistedUseCase,
    coinImageResolver: CoinImageResolver,
    private val observeCoinDetailUseCase: ObserveCoinDetailUseCase,
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase,
    private val mapper: CoinDetailPresentationModelMapper,
) : BaseViewModel<CoinDetailState, CoinDetailEvent, CoinDetailAction>(
    initialState = CoinDetailState(
        symbol = coinName,
        imageUrl = coinImageResolver.getImageUrl(coinName),
    ),
) {
    /**
     * Chart data pipe (vico/Kandle best practice, §Q13): the producer is held by the ViewModel
     * so it survives configuration changes and is fed incrementally off the candle stream.
     * Deliberately outside [CoinDetailState] — chart series use a conflated, async channel.
     */
    val chartProducer = FinancialChartModelProducer()

    private var observeJob: Job? = null
    private var lastChartCandles: List<Candle> = emptyList()

    @AssistedFactory
    interface Factory {
        fun create(coinName: String): CoinDetailViewModel
    }

    init {
        ensureObserving()

        observeIsWatchlistedUseCase(coinName)
            .map { CoinDetailAction.Internal.ReceiveWatchlisted(isWatchlisted = it) }
            .onEach(::sendAction)
            .launchIn(viewModelScope)

        observeMarketRealtimeState()
            .map { CoinDetailAction.Internal.ReceiveRealtimeState(realtimeState = it) }
            .onEach(::sendAction)
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: CoinDetailAction) {
        when (action) {
            CoinDetailAction.BackClick -> handleBackClick()
            CoinDetailAction.FavoriteClick -> handleFavoriteClick()
            CoinDetailAction.ScreenStart -> ensureObserving()
            is CoinDetailAction.Internal.ReceiveCoinDetail -> handleReceiveCoinDetail(action)
            is CoinDetailAction.Internal.ReceiveWatchlisted -> handleReceiveWatchlisted(action)
            is CoinDetailAction.Internal.ReceiveRealtimeState -> handleReceiveRealtimeState(action)
        }
    }

    private fun handleBackClick() {
        sendEvent(CoinDetailEvent.NavigateBack)
    }

    private fun handleFavoriteClick() {
        viewModelScope.launch { toggleWatchlistUseCase(coinName) }
    }

    /**
     * The candle stream feeds [chartProducer], which lives outside [CoinDetailState]. It is fed
     * inside the flow, before the action is posted, so that [handleAction] stays synchronous and
     * chart updates stay ordered with respect to the emissions that produced them.
     */
    private fun ensureObserving() {
        if (observeJob?.isActive == true) return

        observeJob?.cancel()
        observeJob = observeCoinDetailUseCase(symbol = coinName)
            .onEach { result ->
                if (result is ObserveCoinDetailUseCase.Result.Success) feedChart(result.candles)
                sendAction(CoinDetailAction.Internal.ReceiveCoinDetail(result = result))
            }
            .launchIn(viewModelScope)
    }

    private fun handleReceiveCoinDetail(action: CoinDetailAction.Internal.ReceiveCoinDetail) {
        when (val result = action.result) {
            is ObserveCoinDetailUseCase.Result.Loading -> {
                mutableStateFlow.update { it.copy(isLoading = true, errorMsg = null) }
            }

            is ObserveCoinDetailUseCase.Result.Success -> {
                mutableStateFlow.update { state ->
                    mapper
                        .toUiState(
                            vo = result.coinDetail,
                            orderBook = result.orderBook,
                            imageUrl = state.imageUrl,
                            isLoading = false,
                            errorMsg = null,
                        )
                        .copy(isWatchlisted = state.isWatchlisted)
                }
            }

            is ObserveCoinDetailUseCase.Result.Error.Connection -> {
                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = CryptoString
                            .crypto_coin_detail_connection_error_state
                            .asText(),
                    )
                }
                sendEvent(
                    CoinDetailEvent.ShowToast(
                        text = CryptoString.crypto_coin_detail_connection_error_toast.asText(),
                    ),
                )
            }
        }
    }

    private fun handleReceiveWatchlisted(action: CoinDetailAction.Internal.ReceiveWatchlisted) {
        mutableStateFlow.update { it.copy(isWatchlisted = action.isWatchlisted) }
    }

    private fun handleReceiveRealtimeState(action: CoinDetailAction.Internal.ReceiveRealtimeState) {
        mutableStateFlow.update {
            it.copy(realtimeStatusMessage = action.realtimeState.toRealtimeStatusText())
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
}

/**
 * State for the Coin Detail screen.
 */
data class CoinDetailState(
    val symbol: String = "",
    val imageUrl: String = "",
    val currentPrice: String = "",
    val priceChangeText: String = "",
    val priceChangePercent: Double = 0.0,
    val high24h: String = "",
    val low24h: String = "",
    val volume24h: String = "",
    val openPrice: String = "",
    val tickSize: BigDecimal? = null,
    val orderBook: OrderBookUiModel = OrderBookUiModel.EMPTY,
    val isWatchlisted: Boolean = false,
    val isLoading: Boolean = true,
    val errorMsg: Text? = null,
    val realtimeStatusMessage: Text? = null,
)

/**
 * One-shot UI events for the Coin Detail screen.
 */
sealed class CoinDetailEvent {
    data object NavigateBack : CoinDetailEvent()

    data class ShowToast(val text: Text) : CoinDetailEvent()
}

/**
 * User and system actions for the Coin Detail screen.
 */
sealed class CoinDetailAction {
    data object BackClick : CoinDetailAction()

    data object FavoriteClick : CoinDetailAction()

    data object ScreenStart : CoinDetailAction()

    /**
     * Internal actions dispatched by the ViewModel from coroutines.
     */
    sealed class Internal : CoinDetailAction() {
        data class ReceiveCoinDetail(val result: ObserveCoinDetailUseCase.Result) : Internal()

        data class ReceiveWatchlisted(val isWatchlisted: Boolean) : Internal()

        data class ReceiveRealtimeState(val realtimeState: MarketRealtimeState) : Internal()
    }
}

data class OrderBookUiModel(
    val rows: List<OrderBookRowUiModel>,
    val bidPercentText: String,
    val askPercentText: String,
    val bidRatio: Float,
    val tickSizeText: String,
) {
    companion object {
        val EMPTY = OrderBookUiModel(
            rows = List(ORDER_BOOK_ROW_COUNT) { OrderBookRowUiModel(bid = null, ask = null) },
            bidPercentText = "",
            askPercentText = "",
            bidRatio = 0.5f,
            tickSizeText = "",
        )
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
