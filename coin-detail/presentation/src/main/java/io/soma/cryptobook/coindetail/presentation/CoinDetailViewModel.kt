package io.soma.cryptobook.coindetail.presentation

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.coindetail.domain.usecase.ObserveCoinDetailUseCase
import io.soma.cryptobook.coindetail.domain.usecase.ObserveIsWatchlistedUseCase
import io.soma.cryptobook.coindetail.domain.usecase.ToggleWatchlistUseCase
import io.soma.cryptobook.coindetail.presentation.CoinDetailContract.Effect
import io.soma.cryptobook.coindetail.presentation.CoinDetailContract.Event
import io.soma.cryptobook.coindetail.presentation.CoinDetailContract.State
import io.soma.cryptobook.coindetail.presentation.CoinDetailContract.ViewModel
import io.soma.cryptobook.coindetail.presentation.mapper.CoinDetailPresentationModelMapper
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
    private var observeJob: Job? = null

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
                        updateState { state ->
                            mapper.toUiState(
                                vo = result.coinDetail,
                                candles = result.candles,
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
                                    .cb_coin_detail_connection_error_state
                                    .asText(),
                            )
                        }
                        messageHelper.showToast(
                            CryptoString.cb_coin_detail_connection_error_toast.asText(),
                        )
                    }
                }
            }
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

        MarketRealtimeState.Recovering -> CryptoString.cb_realtime_recovering.asText()

        is MarketRealtimeState.Failed -> CryptoString.cb_realtime_disconnected.asText()
    }
}
