package io.soma.cryptobook.home.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.domain.error.CoinPriceError
import io.soma.cryptobook.core.domain.image.CoinImageResolver
import io.soma.cryptobook.core.domain.message.MessageHelper
import io.soma.cryptobook.core.domain.model.CoinSortColumn
import io.soma.cryptobook.core.domain.model.CoinSortState
import io.soma.cryptobook.core.domain.model.next
import io.soma.cryptobook.core.domain.navigation.AppPage
import io.soma.cryptobook.core.domain.navigation.NavigationHelper
import io.soma.cryptobook.core.domain.outcome.handle
import io.soma.cryptobook.core.domain.usecase.MarketRealtimeState
import io.soma.cryptobook.core.domain.usecase.ObserveMarketRealtimeState
import io.soma.cryptobook.core.domain.usecase.ObserveSortedCoinListUseCase
import io.soma.cryptobook.core.presentation.MviViewModel
import io.soma.cryptobook.home.domain.usecase.ObserveCoinListUseCase
import io.soma.cryptobook.home.domain.usecase.ObserveCoinSortUseCase
import io.soma.cryptobook.home.domain.usecase.SetCoinSortUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val observeCoinListUseCase: ObserveCoinListUseCase,
    private val observeCoinSortUseCase: ObserveCoinSortUseCase,
    private val setCoinSortUseCase: SetCoinSortUseCase,
    private val observeSortedCoinListUseCase: ObserveSortedCoinListUseCase,
    private val coinImageResolver: CoinImageResolver,
    private val navigationHelper: NavigationHelper,
    private val messageHelper: MessageHelper,
    private val observeMarketRealtimeState: ObserveMarketRealtimeState,
) : MviViewModel<HomeEvent, HomeUiState, HomeSideEffect>(HomeUiState()) {
    private var observeJob: Job? = null

    init {
        observeRealtimeState()
        observeSortState()
        observeCoins()
    }

    override fun handleEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.OnRefresh -> observeCoins()

            HomeEvent.OnBackClicked -> navigationHelper.back()

            is HomeEvent.OnCoinClicked -> navigationHelper.navigate(
                AppPage.CoinDetail(event.symbol),
            )

            HomeEvent.SearchIconClick -> navigationHelper.navigate(AppPage.Search)

            is HomeEvent.OnSortClick -> onSortClick(event.column)
        }
    }

    private fun observeCoins() {
        observeJob?.cancel()
        reduce { copy(isLoading = true) }
        observeJob = viewModelScope.launch {
            observeSortedCoinListUseCase(
                prices = observeCoinListUseCase(),
                sort = observeCoinSortUseCase(),
            ).collect { outcome ->
                outcome.handle(
                    onSuccess = { coins ->
                        reduce {
                            copy(
                                isLoading = false,
                                errorMsg = null,
                                coins = coins.map {
                                    it.toCoinItem(coinImageResolver.getImageUrl(it.symbol))
                                },
                            )
                        }
                    },
                    onFailure = { error ->
                        val message = context.getString(error.toMessageRes())
                        reduce {
                            copy(isLoading = false, errorMsg = message)
                        }
                        messageHelper.showToast(message)
                    },
                )
            }
        }
    }

    private fun observeSortState() {
        viewModelScope.launch {
            observeCoinSortUseCase().collect { sort ->
                reduce {
                    copy(sortColumn = sort.column, sortDirection = sort.direction)
                }
            }
        }
    }

    private fun onSortClick(column: CoinSortColumn) {
        val next = CoinSortState(currentState.sortColumn, currentState.sortDirection).next(column)
        viewModelScope.launch { setCoinSortUseCase(next.column, next.direction) }
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

        MarketRealtimeState.Recovering -> context.getString(
            CryptoString.cb_realtime_recovering,
        )

        is MarketRealtimeState.Failed -> context.getString(
            CryptoString.cb_realtime_disconnected,
        )
    }

    private fun CoinPriceError.toMessageRes(): Int = when (this) {
        CoinPriceError.Network -> CryptoString.cb_error_network
        CoinPriceError.RateLimited -> CryptoString.cb_error_rate_limited
        CoinPriceError.Server -> CryptoString.cb_error_server
        CoinPriceError.UnexpectedResponse -> CryptoString.cb_error_unexpected_response
        is CoinPriceError.Unknown -> CryptoString.cb_error_unknown
    }
}
