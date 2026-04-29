package io.soma.cryptobook.home.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.core.domain.error.CoinPriceError
import io.soma.cryptobook.core.domain.image.CoinImageResolver
import io.soma.cryptobook.core.domain.message.MessageHelper
import io.soma.cryptobook.core.domain.navigation.AppPage
import io.soma.cryptobook.core.domain.navigation.NavigationHelper
import io.soma.cryptobook.core.domain.outcome.handle
import io.soma.cryptobook.core.domain.usecase.MarketRealtimeState
import io.soma.cryptobook.core.domain.usecase.ObserveMarketRealtimeState
import io.soma.cryptobook.core.presentation.MviViewModel
import io.soma.cryptobook.home.domain.usecase.ObserveCoinListUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeCoinListUseCase: ObserveCoinListUseCase,
    private val coinImageResolver: CoinImageResolver,
    private val navigationHelper: NavigationHelper,
    private val messageHelper: MessageHelper,
    private val observeMarketRealtimeState: ObserveMarketRealtimeState,
) : MviViewModel<HomeEvent, HomeUiState, HomeSideEffect>(HomeUiState()) {
    private var observeJob: Job? = null

    init {
        observeCoins()
        observeRealtimeState()
    }

    override fun handleEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.OnRefresh -> {
                observeCoins()
            }

            HomeEvent.OnBackClicked -> navigationHelper.back()
            is HomeEvent.OnCoinClicked -> navigationHelper.navigate(
                AppPage.CoinDetail(event.symbol),
            )
        }
    }

    private fun observeCoins() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observeCoinListUseCase().collect { outcome ->
                outcome.handle(
                    onSuccess = { coins ->
                        reduce {
                            copy(
                                isLoading = coins.isEmpty(),
                                errorMsg = null,
                                coins = coins.map {
                                    it.toCoinItem(coinImageResolver.getImageUrl(it.symbol))
                                },
                            )
                        }
                    },
                    onFailure = { error ->
                        val message = error.toHomeErrorMessage()
                        reduce {
                            copy(
                                isLoading = false,
                                errorMsg = message,
                            )
                        }
                        messageHelper.showToast(message)
                    },
                )
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

    private fun CoinPriceError.toHomeErrorMessage(): String {
        return when (this) {
            CoinPriceError.Network -> "네트워크 연결을 확인해주세요"
            CoinPriceError.RateLimited -> "요청이 너무 많습니다. 잠시 후 다시 시도해주세요"
            CoinPriceError.Server -> "가격 정보를 불러오지 못했습니다"
            CoinPriceError.UnexpectedResponse -> "가격 정보 응답이 올바르지 않습니다"
            is CoinPriceError.Unknown -> "알 수 없는 오류가 발생했습니다"
        }
    }
}
