package io.soma.cryptobook.coindetail.presentation

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.coindetail.domain.usecase.ObserveCoinDetailUseCase
import io.soma.cryptobook.coindetail.presentation.mapper.CoinDetailPresentationModelMapper
import io.soma.cryptobook.core.domain.image.CoinImageResolver
import io.soma.cryptobook.core.domain.message.MessageHelper
import io.soma.cryptobook.core.domain.navigation.NavigationHelper
import io.soma.cryptobook.core.domain.usecase.MarketRealtimeState
import io.soma.cryptobook.core.domain.usecase.ObserveMarketRealtimeState
import io.soma.cryptobook.core.presentation.MviViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CoinDetailViewModel.Factory::class)
class CoinDetailViewModel @AssistedInject constructor(
    @Assisted private val coinName: String,
    private val observeCoinDetailUseCase: ObserveCoinDetailUseCase,
    private val mapper: CoinDetailPresentationModelMapper,
    private val coinImageResolver: CoinImageResolver,
    private val navigationHelper: NavigationHelper,
    private val messageHelper: MessageHelper,
    private val observeMarketRealtimeState: ObserveMarketRealtimeState,
) : MviViewModel<CoinDetailEvent, CoinDetailUiState, CoinDetailSideEffect>(
    CoinDetailUiState(
        symbol = coinName,
        imageUrl = coinImageResolver.getImageUrl(coinName),
    ),
) {
    private var observeJob: Job? = null

    @AssistedFactory
    interface Factory {
        fun create(coinName: String): CoinDetailViewModel
    }

    init {
        ensureObserving()
        observeRealtimeState()
    }

    override fun handleEvent(event: CoinDetailEvent) {
        when (event) {
            CoinDetailEvent.OnBackClicked -> {
                navigationHelper.back()
            }

            CoinDetailEvent.OnScreenStarted -> ensureObserving()
        }
    }

    private fun ensureObserving() {
        if (observeJob?.isActive == true) return

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observeCoinDetailUseCase(symbol = coinName).collect { result ->
                when (result) {
                    is ObserveCoinDetailUseCase.Result.Loading -> {
                        reduce { copy(isLoading = true, errorMsg = null) }
                    }

                    is ObserveCoinDetailUseCase.Result.Success -> {
                        reduce {
                            mapper.toUiState(
                                vo = result.coinDetail,
                                candles = result.candles,
                                imageUrl = imageUrl,
                                isLoading = false,
                                errorMsg = null,
                            )
                        }
                    }

                    is ObserveCoinDetailUseCase.Result.Error.Connection -> {
                        reduce { copy(isLoading = false, errorMsg = "연결 오류") }
                        messageHelper.showToast("연결 오류가 발생했습니다")
                    }
                }
            }
        }
    }

    private fun observeRealtimeState() {
        viewModelScope.launch {
            observeMarketRealtimeState().collect { runtimeState ->
                reduce {
                    copy(realtimeStatusMessage = runtimeState.toRealtimeStatusMessage())
                }
            }
        }
    }

    private fun MarketRealtimeState.toRealtimeStatusMessage(): String? = when (this) {
        MarketRealtimeState.Connected,
        MarketRealtimeState.Connecting,
        MarketRealtimeState.Inactive,
        -> null

        MarketRealtimeState.Recovering -> "실시간 연결을 복구하는 중입니다"
        is MarketRealtimeState.Failed -> "실시간 데이터 연결이 중단되었습니다"
    }
}
