package io.soma.cryptobook.watchlist.presentation

import io.soma.cryptobook.core.designsystem.util.Text
import io.soma.cryptobook.core.domain.model.CoinPriceVO
import io.soma.cryptobook.core.domain.model.CoinSortColumn
import io.soma.cryptobook.core.domain.model.CoinSortDirection
import io.soma.cryptobook.core.presentation.Event
import io.soma.cryptobook.core.presentation.SideEffect
import io.soma.cryptobook.core.presentation.UiState
import java.math.BigDecimal

data class WatchlistUiState(
    val coins: List<CoinItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: Text? = null,
    val realtimeStatusMessage: Text? = null,
    val sortColumn: CoinSortColumn = CoinSortColumn.NONE,
    val sortDirection: CoinSortDirection = CoinSortDirection.NONE,
) : UiState

data class CoinItem(
    val symbol: String,
    val imageUrl: String,
    val price: BigDecimal,
    val priceChangePercentage24h: Double,
    val tickSize: BigDecimal? = null,
)

sealed interface WatchlistEvent : Event {
    data object OnRefresh : WatchlistEvent
    data class OnCoinClicked(val symbol: String) : WatchlistEvent
    data class OnSortClick(val column: CoinSortColumn) : WatchlistEvent
}

sealed interface WatchlistSideEffect : SideEffect

fun CoinPriceVO.toCoinItem(imageUrl: String) = CoinItem(
    symbol = symbol,
    imageUrl = imageUrl,
    price = price,
    priceChangePercentage24h = priceChangePercentage24h,
    tickSize = tickSize,
)
