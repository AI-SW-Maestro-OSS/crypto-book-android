package io.soma.cryptobook.home.presentation

import io.soma.cryptobook.core.domain.model.CoinPriceVO
import io.soma.cryptobook.core.presentation.Event
import io.soma.cryptobook.core.presentation.SideEffect
import io.soma.cryptobook.core.presentation.UiState
import io.soma.cryptobook.home.presentation.component.sortheader.SortDirection
import java.math.BigDecimal

data class HomeUiState(
    val coins: List<CoinItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val sortField: SortField = SortField.Price,
    val sortDirection: SortDirection = SortDirection.Desc,
) : UiState

data class CoinItem(
    val symbol: String,
    val price: BigDecimal,
    val priceChangePercentage24h: Double,
)

enum class SortField {
    Symbol,
    Price,
    Change,
}

sealed interface HomeEvent : Event {
    data object OnRefresh : HomeEvent
    data object OnBackClicked : HomeEvent
    data class OnCoinClicked(val symbol: String) : HomeEvent
    data class OnSortClicked(val field: SortField) : HomeEvent
}

sealed interface HomeSideEffect : SideEffect

fun CoinPriceVO.toCoinItem() = CoinItem(
    symbol = symbol,
    price = price,
    priceChangePercentage24h = priceChangePercentage24h,
)
