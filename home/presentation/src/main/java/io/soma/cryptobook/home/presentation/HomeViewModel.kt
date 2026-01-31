package io.soma.cryptobook.home.presentation

import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.core.domain.image.CoinImageResolver
import io.soma.cryptobook.core.domain.message.MessageHelper
import io.soma.cryptobook.core.domain.navigation.AppPage
import io.soma.cryptobook.core.domain.navigation.NavigationHelper
import io.soma.cryptobook.core.presentation.MviViewModel
import io.soma.cryptobook.home.domain.usecase.ObserveCoinListUseCase
import io.soma.cryptobook.home.presentation.component.sortheader.SortDirection
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
            is HomeEvent.OnSortClicked -> updateSort(event.field)
        }
    }

    private fun observeCoins() {
        intent {
            observeCoinListUseCase().collect { result ->
                when (result) {
                    is ObserveCoinListUseCase.Result.Success -> {
                        val sortedCoins = sortCoins(
                            coins = result.coinList.map { it.toCoinItem() },
                            sortField = currentState.sortField,
                            sortDirection = currentState.sortDirection,
                        )
                        reduce {
                            copy(
                                isLoading = false,
                                errorMsg = null,
                                coins = sortedCoins.coinList.map {
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
                        reduce { copy(errorMsg = "연결 끊김") }
                        messageHelper.showToast("실시간 연결이 끊겼습니다")
                    }
                }
            }
        }
    }

    private fun updateSort(field: SortField) {
        val newDirection = if (field == currentState.sortField) {
            toggleDirection(currentState.sortDirection)
        } else {
            defaultSortDirection(field)
        }

        reduce {
            copy(
                sortField = field,
                sortDirection = newDirection,
                coins = sortCoins(coins, field, newDirection),
            )
        }
    }

    private fun sortCoins(
        coins: List<CoinItem>,
        sortField: SortField,
        sortDirection: SortDirection,
    ): List<CoinItem> {
        val comparator = when (sortField) {
            SortField.Symbol -> compareBy<CoinItem> { it.symbol }
            SortField.Price -> compareBy<CoinItem> { it.price }
            SortField.Change -> compareBy<CoinItem> { it.priceChangePercentage24h }
        }

        return if (sortDirection == SortDirection.Desc) {
            coins.sortedWith(comparator).asReversed()
        }else{
            coins.sortedWith(comparator)
        }
    }

    private fun toggleDirection(current: SortDirection): SortDirection {
        return if (current == SortDirection.Asc) SortDirection.Desc else SortDirection.Asc
    }

    private fun defaultSortDirection(field: SortField): SortDirection {
        return when (field) {
            SortField.Symbol -> SortDirection.Asc
            SortField.Price, SortField.Change -> SortDirection.Desc
        }
    }
}
