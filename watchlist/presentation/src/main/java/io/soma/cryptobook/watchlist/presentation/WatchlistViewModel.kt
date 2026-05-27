package io.soma.cryptobook.watchlist.presentation

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
import io.soma.cryptobook.watchlist.domain.usecase.ObserveWatchlistSortUseCase
import io.soma.cryptobook.watchlist.domain.usecase.ObserveWatchlistUseCase
import io.soma.cryptobook.watchlist.domain.usecase.SetWatchlistSortUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val observeWatchlistUseCase: ObserveWatchlistUseCase,
    private val observeWatchlistSortUseCase: ObserveWatchlistSortUseCase,
    private val setWatchlistSortUseCase: SetWatchlistSortUseCase,
    private val observeSortedCoinListUseCase: ObserveSortedCoinListUseCase,
    private val coinImageResolver: CoinImageResolver,
    private val navigationHelper: NavigationHelper,
    private val messageHelper: MessageHelper,
    private val observeMarketRealtimeState: ObserveMarketRealtimeState,
) : MviViewModel<WatchlistEvent, WatchlistUiState, WatchlistSideEffect>(WatchlistUiState()) {
    private var observeJob: Job? = null

    init {
        observeRealtimeState()
        observeSortState()
        observeCoins()
    }

    override fun handleEvent(event: WatchlistEvent) {
        when (event) {
            WatchlistEvent.OnRefresh -> observeCoins()

            is WatchlistEvent.OnCoinClicked -> navigationHelper.navigate(
                AppPage.CoinDetail(event.symbol),
            )

            is WatchlistEvent.OnSortClick -> onSortClick(event.column)
        }
    }

    private fun observeCoins() {
        observeJob?.cancel()
        reduce { copy(isLoading = true) }
        observeJob = viewModelScope.launch {
            observeSortedCoinListUseCase(
                prices = observeWatchlistUseCase(),
                sort = observeWatchlistSortUseCase(),
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
            observeWatchlistSortUseCase().collect { sort ->
                reduce {
                    copy(sortColumn = sort.column, sortDirection = sort.direction)
                }
            }
        }
    }

    private fun onSortClick(column: CoinSortColumn) {
        val next = CoinSortState(currentState.sortColumn, currentState.sortDirection).next(column)
        viewModelScope.launch { setWatchlistSortUseCase(next.column, next.direction) }
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
