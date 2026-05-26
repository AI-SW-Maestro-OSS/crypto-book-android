package io.soma.cryptobook.coindetail.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.soma.cryptobook.coindetail.domain.usecase.ObserveCoinDetailUseCase
import io.soma.cryptobook.coindetail.presentation.CoinDetailContract.Effect
import io.soma.cryptobook.coindetail.presentation.CoinDetailContract.Event
import io.soma.cryptobook.coindetail.presentation.CoinDetailContract.State
import io.soma.cryptobook.coindetail.presentation.CoinDetailContract.ViewModel
import io.soma.cryptobook.coindetail.presentation.mapper.CoinDetailPresentationModelMapper
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.domain.image.CoinImageResolver
import io.soma.cryptobook.core.domain.message.MessageHelper
import io.soma.cryptobook.core.domain.usecase.MarketRealtimeState
import io.soma.cryptobook.core.domain.usecase.ObserveMarketRealtimeState
import io.soma.cryptobook.core.presentation.mvi.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CoinDetailViewModel.Factory::class)
class CoinDetailViewModel @AssistedInject constructor(
    @Assisted private val coinName: String,
    @ApplicationContext private val context: Context,
    private val observeCoinDetailUseCase: ObserveCoinDetailUseCase,
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
    }

    override fun event(event: Event) {
        when (event) {
            Event.OnBackClicked -> emitEffect(Effect.NavigateBack)
            Event.OnScreenStarted -> ensureObserving()
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
                                imageUrl = state.imageUrl,
                                isLoading = false,
                                errorMsg = null,
                            )
                        }
                    }

                    is ObserveCoinDetailUseCase.Result.Error.Connection -> {
                        updateState { state ->
                            state.copy(
                                isLoading = false,
                                errorMsg = context.getString(
                                    CryptoString.cb_coin_detail_connection_error_state,
                                ),
                            )
                        }
                        messageHelper.showToast(
                            context.getString(
                                CryptoString.cb_coin_detail_connection_error_toast,
                            ),
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
                    state.copy(realtimeStatusMessage = runtimeState.toRealtimeStatusMessage())
                }
            }
        }
    }

    private fun MarketRealtimeState.toRealtimeStatusMessage(): String? = when (this) {
        MarketRealtimeState.Connected,
        MarketRealtimeState.Connecting,
        MarketRealtimeState.Inactive,
        -> null

        MarketRealtimeState.Recovering -> context.getString(
            CryptoString.cb_realtime_recovering,
        )

        is MarketRealtimeState.Failed -> context.getString(
            CryptoString.cb_realtime_disconnected,
        )
    }
}
