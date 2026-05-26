package io.soma.cryptobook.home.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.domain.error.CoinPriceError
import io.soma.cryptobook.core.domain.image.CoinImageResolver
import io.soma.cryptobook.core.domain.message.MessageHelper
import io.soma.cryptobook.core.domain.model.CoinPriceVO
import io.soma.cryptobook.core.domain.model.CoinSortColumn
import io.soma.cryptobook.core.domain.model.CoinSortDirection
import io.soma.cryptobook.core.domain.navigation.AppPage
import io.soma.cryptobook.core.domain.navigation.NavigationHelper
import io.soma.cryptobook.core.domain.outcome.handle
import io.soma.cryptobook.core.domain.usecase.MarketRealtimeState
import io.soma.cryptobook.core.domain.usecase.ObserveMarketRealtimeState
import io.soma.cryptobook.core.presentation.MviViewModel
import io.soma.cryptobook.home.domain.usecase.ObserveCoinListUseCase
import io.soma.cryptobook.home.domain.usecase.ObserveCoinSortUseCase
import io.soma.cryptobook.home.domain.usecase.SetCoinSortUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val observeCoinListUseCase: ObserveCoinListUseCase,
    private val observeCoinSortUseCase: ObserveCoinSortUseCase,
    private val setCoinSortUseCase: SetCoinSortUseCase,
    private val coinImageResolver: CoinImageResolver,
    private val navigationHelper: NavigationHelper,
    private val messageHelper: MessageHelper,
    private val observeMarketRealtimeState: ObserveMarketRealtimeState,
) : MviViewModel<HomeEvent, HomeUiState, HomeSideEffect>(HomeUiState()) {
    private var observeJob: Job? = null

    private var sortColumn: CoinSortColumn = CoinSortColumn.NONE
    private var sortDirection: CoinSortDirection = CoinSortDirection.NONE
    private var latestCoins: List<CoinPriceVO> = emptyList()

    /** Symbol order frozen at the last sort change; null until established for the current session. */
    private var frozenOrder: List<String>? = null

    init {
        observeRealtimeState()
        viewModelScope.launch {
            val initialSort = observeCoinSortUseCase().first()
            sortColumn = initialSort.column
            sortDirection = initialSort.direction
            reduce {
                copy(
                    sortColumn = initialSort.column,
                    sortDirection = initialSort.direction,
                )
            }
            observeCoins()
        }
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

            HomeEvent.SearchIconClick -> navigationHelper.navigate(
                AppPage.Search,
            )

            is HomeEvent.OnSortClick -> onSortClick(event.column)
        }
    }

    private fun observeCoins() {
        observeJob?.cancel()
        frozenOrder = null
        observeJob = viewModelScope.launch {
            observeCoinListUseCase().collect { outcome ->
                outcome.handle(
                    onSuccess = { coins ->
                        latestCoins = coins
                        val ordered = orderCoins(coins)
                        reduce {
                            copy(
                                isLoading = coins.isEmpty(),
                                errorMsg = null,
                                coins = ordered.map {
                                    it.toCoinItem(coinImageResolver.getImageUrl(it.symbol))
                                },
                            )
                        }
                    },
                    onFailure = { error ->
                        val message = context.getString(error.toMessageRes())
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

    private fun onSortClick(column: CoinSortColumn) {
        val (nextColumn, nextDirection) = nextSortState(column)
        sortColumn = nextColumn
        sortDirection = nextDirection
        frozenOrder = null
        val ordered = orderCoins(latestCoins)
        reduce {
            copy(
                sortColumn = nextColumn,
                sortDirection = nextDirection,
                coins = ordered.map {
                    it.toCoinItem(coinImageResolver.getImageUrl(it.symbol))
                },
            )
        }
        viewModelScope.launch { setCoinSortUseCase(nextColumn, nextDirection) }
    }

    private fun nextSortState(column: CoinSortColumn): Pair<CoinSortColumn, CoinSortDirection> =
        if (column == sortColumn) {
            when (sortDirection) {
                CoinSortDirection.NONE -> column to CoinSortDirection.ASC
                CoinSortDirection.ASC -> column to CoinSortDirection.DESC
                CoinSortDirection.DESC -> CoinSortColumn.NONE to CoinSortDirection.NONE
            }
        } else {
            column to CoinSortDirection.ASC
        }

    /**
     * Reorders by the frozen symbol order so live updates don't reshuffle rows.
     * The order is (re)computed only when [frozenOrder] is null — i.e. on a sort change
     * or the first emission of an observe session. New symbols are appended at the end.
     */
    private fun orderCoins(coins: List<CoinPriceVO>): List<CoinPriceVO> {
        if (sortColumn == CoinSortColumn.NONE || sortDirection == CoinSortDirection.NONE) {
            frozenOrder = null
            return coins
        }
        val order = frozenOrder ?: computeSortedSymbols(coins).also { frozenOrder = it }
        val bySymbol = coins.associateBy { it.symbol }
        val known = order.toHashSet()
        return order.mapNotNull { bySymbol[it] } + coins.filter { it.symbol !in known }
    }

    private fun computeSortedSymbols(coins: List<CoinPriceVO>): List<String> {
        val base: Comparator<CoinPriceVO> = when (sortColumn) {
            CoinSortColumn.SYMBOL -> compareBy { it.symbol }
            CoinSortColumn.PRICE -> compareBy { it.price }
            CoinSortColumn.CHANGE -> compareBy { it.priceChangePercentage24h }
            CoinSortColumn.VOLUME -> compareBy { it.quoteVolume }
            CoinSortColumn.NONE -> return coins.map { it.symbol }
        }
        val directed = if (sortDirection == CoinSortDirection.DESC) base.reversed() else base
        return coins.sortedWith(directed.thenBy { it.symbol }).map { it.symbol }
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
