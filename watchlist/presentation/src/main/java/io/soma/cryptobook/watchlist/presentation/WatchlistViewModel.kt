package io.soma.cryptobook.watchlist.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.util.Text
import io.soma.cryptobook.core.designsystem.util.asText
import io.soma.cryptobook.core.domain.error.CoinPriceError
import io.soma.cryptobook.core.domain.image.CoinImageResolver
import io.soma.cryptobook.core.domain.model.CoinPriceVO
import io.soma.cryptobook.core.domain.model.CoinSortColumn
import io.soma.cryptobook.core.domain.model.CoinSortDirection
import io.soma.cryptobook.core.domain.model.CoinSortState
import io.soma.cryptobook.core.domain.model.next
import io.soma.cryptobook.core.domain.outcome.Outcome
import io.soma.cryptobook.core.domain.outcome.handle
import io.soma.cryptobook.core.domain.usecase.GetUserDataUseCase
import io.soma.cryptobook.core.domain.usecase.MarketRealtimeState
import io.soma.cryptobook.core.domain.usecase.ObserveMarketRealtimeState
import io.soma.cryptobook.core.domain.usecase.ObserveSortedCoinListUseCase
import io.soma.cryptobook.core.ui.BaseViewModel
import io.soma.cryptobook.watchlist.domain.usecase.ObserveWatchlistSortUseCase
import io.soma.cryptobook.watchlist.domain.usecase.ObserveWatchlistUseCase
import io.soma.cryptobook.watchlist.domain.usecase.SetWatchlistSortUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

/**
 * ViewModel for the Watchlist screen.
 */
@HiltViewModel
class WatchlistViewModel @Inject constructor(
    observeWatchlistUseCase: ObserveWatchlistUseCase,
    observeWatchlistSortUseCase: ObserveWatchlistSortUseCase,
    observeSortedCoinListUseCase: ObserveSortedCoinListUseCase,
    observeMarketRealtimeState: ObserveMarketRealtimeState,
    getUserDataUseCase: GetUserDataUseCase,
    private val setWatchlistSortUseCase: SetWatchlistSortUseCase,
    private val coinImageResolver: CoinImageResolver,
) : BaseViewModel<WatchlistState, WatchlistEvent, WatchlistAction>(
    initialState = WatchlistState(isLoading = true),
) {

    init {
        combine(
            observeSortedCoinListUseCase(
                prices = observeWatchlistUseCase(),
                sort = observeWatchlistSortUseCase(),
            ),
            getUserDataUseCase(),
        ) { outcome, userData ->
            WatchlistAction.Internal.ReceiveCoinList(
                outcome = outcome,
                usdKrwExchangeRate = userData.usdKrwExchangeRate,
            )
        }
            .onEach(::sendAction)
            .launchIn(viewModelScope)

        observeWatchlistSortUseCase()
            .map { WatchlistAction.Internal.ReceiveCoinSort(sort = it) }
            .onEach(::sendAction)
            .launchIn(viewModelScope)

        observeMarketRealtimeState()
            .map { WatchlistAction.Internal.ReceiveRealtimeState(realtimeState = it) }
            .onEach(::sendAction)
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: WatchlistAction) {
        when (action) {
            is WatchlistAction.CoinClick -> handleCoinClick(action)
            is WatchlistAction.SortClick -> handleSortClick(action)
            is WatchlistAction.Internal.ReceiveCoinList -> handleReceiveCoinList(action)
            is WatchlistAction.Internal.ReceiveCoinSort -> handleReceiveCoinSort(action)
            is WatchlistAction.Internal.ReceiveRealtimeState -> handleReceiveRealtimeState(action)
        }
    }

    private fun handleCoinClick(action: WatchlistAction.CoinClick) {
        sendEvent(WatchlistEvent.NavigateToCoinDetail(symbol = action.symbol))
    }

    private fun handleSortClick(action: WatchlistAction.SortClick) {
        val next = CoinSortState(column = state.sortColumn, direction = state.sortDirection)
            .next(action.column)
        viewModelScope.launch { setWatchlistSortUseCase(next.column, next.direction) }
    }

    private fun handleReceiveCoinList(action: WatchlistAction.Internal.ReceiveCoinList) {
        action.outcome.handle(
            onSuccess = { coins ->
                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = null,
                        coins = coins.map { coin ->
                            coin.toCoinItem(coinImageResolver.getImageUrl(coin.symbol))
                        },
                        usdKrwExchangeRate = action.usdKrwExchangeRate,
                    )
                }
            },
            onFailure = { error ->
                val text = error.toText()
                mutableStateFlow.update { it.copy(isLoading = false, errorMsg = text) }
                sendEvent(WatchlistEvent.ShowToast(text = text))
            },
        )
    }

    private fun handleReceiveCoinSort(action: WatchlistAction.Internal.ReceiveCoinSort) {
        mutableStateFlow.update {
            it.copy(sortColumn = action.sort.column, sortDirection = action.sort.direction)
        }
    }

    private fun handleReceiveRealtimeState(action: WatchlistAction.Internal.ReceiveRealtimeState) {
        mutableStateFlow.update {
            it.copy(realtimeStatusMessage = action.realtimeState.toRealtimeStatusText())
        }
    }
}

/**
 * State for the Watchlist screen.
 */
data class WatchlistState(
    val coins: List<CoinItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: Text? = null,
    val realtimeStatusMessage: Text? = null,
    val sortColumn: CoinSortColumn = CoinSortColumn.NONE,
    val sortDirection: CoinSortDirection = CoinSortDirection.NONE,
    val usdKrwExchangeRate: BigDecimal = BigDecimal.ZERO,
)

/**
 * One-shot UI events for the Watchlist screen.
 */
sealed class WatchlistEvent {
    data class NavigateToCoinDetail(val symbol: String) : WatchlistEvent()

    data class ShowToast(val text: Text) : WatchlistEvent()
}

/**
 * User and system actions for the Watchlist screen.
 */
sealed class WatchlistAction {
    data class CoinClick(val symbol: String) : WatchlistAction()

    data class SortClick(val column: CoinSortColumn) : WatchlistAction()

    /**
     * Internal actions dispatched by the ViewModel from coroutines.
     */
    sealed class Internal : WatchlistAction() {
        data class ReceiveCoinList(
            val outcome: Outcome<List<CoinPriceVO>, CoinPriceError>,
            val usdKrwExchangeRate: BigDecimal,
        ) : Internal()

        data class ReceiveCoinSort(val sort: CoinSortState) : Internal()

        data class ReceiveRealtimeState(val realtimeState: MarketRealtimeState) : Internal()
    }
}

/**
 * A single coin row rendered by the Watchlist screen.
 */
data class CoinItem(
    val symbol: String,
    val imageUrl: String,
    val price: BigDecimal,
    val priceChangePercentage24h: Double,
    val tickSize: BigDecimal? = null,
    val quoteVolume: BigDecimal = BigDecimal.ZERO,
)

fun CoinPriceVO.toCoinItem(imageUrl: String) = CoinItem(
    symbol = symbol,
    imageUrl = imageUrl,
    price = price,
    priceChangePercentage24h = priceChangePercentage24h,
    tickSize = tickSize,
    quoteVolume = quoteVolume,
)

private fun MarketRealtimeState.toRealtimeStatusText(): Text? = when (this) {
    MarketRealtimeState.Connected,
    MarketRealtimeState.Connecting,
    MarketRealtimeState.Inactive,
    -> null

    MarketRealtimeState.Recovering -> CryptoString.crypto_realtime_recovering.asText()

    is MarketRealtimeState.Failed -> CryptoString.crypto_realtime_disconnected.asText()
}

private fun CoinPriceError.toText(): Text = when (this) {
    CoinPriceError.Network -> CryptoString.crypto_error_network.asText()
    CoinPriceError.RateLimited -> CryptoString.crypto_error_rate_limited.asText()
    CoinPriceError.Server -> CryptoString.crypto_error_server.asText()
    CoinPriceError.UnexpectedResponse -> CryptoString.crypto_error_unexpected_response.asText()
    is CoinPriceError.Unknown -> CryptoString.crypto_error_unknown.asText()
}
