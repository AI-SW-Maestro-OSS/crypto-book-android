package io.soma.cryptobook.home.presentation

import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.core.domain.image.CoinImageResolver
import io.soma.cryptobook.core.domain.message.MessageHelper
import io.soma.cryptobook.core.domain.navigation.AppPage
import io.soma.cryptobook.core.domain.navigation.NavigationHelper
import io.soma.cryptobook.core.presentation.MviViewModel
import io.soma.cryptobook.home.domain.usecase.ObserveCoinListUseCase
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeCoinListUseCase: ObserveCoinListUseCase,
    private val coinImageResolver: CoinImageResolver,
    private val navigationHelper: NavigationHelper,
    private val messageHelper: MessageHelper,
) : MviViewModel<HomeEvent, HomeUiState, HomeSideEffect>(HomeUiState()) {

    init {
        observeCoins()
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
        intent {
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

                    is ObserveCoinListUseCase.Result.Error.Disconnected -> {
                        reduce { copy(isLoading = true, errorMsg = null) }
                        messageHelper.showToast("실시간 연결이 끊겼습니다")
                    }
                }
            }
        }
    }
}
