package io.soma.cryptobook.home.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.core.domain.image.CoinImageResolver
import io.soma.cryptobook.core.domain.message.MessageHelper
import io.soma.cryptobook.core.domain.navigation.AppPage
import io.soma.cryptobook.core.domain.navigation.NavigationHelper
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
            observeCoinListUseCase().collect { result ->
                when (result) {
                    is ObserveCoinListUseCase.Result.Success -> {
                        reduce {
                            copy(
                                isLoading = result.coinList.isEmpty(),
                                errorMsg = null,
                                coins = result.coinList.map {
                                    it.toCoinItem(coinImageResolver.getImageUrl(it.symbol))
                                },
                            )
                        }
                    }

                    is ObserveCoinListUseCase.Result.Error.Connection -> {
                        reduce { copy(errorMsg = "연결 오류") }
                        messageHelper.showToast("실시간 연결 오류")
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
