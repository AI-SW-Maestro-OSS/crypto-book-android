package io.soma.cryptobook.home.presentation

import io.soma.cryptobook.core.domain.model.CoinPriceVO
import io.soma.cryptobook.core.presentation.Event
import io.soma.cryptobook.core.presentation.SideEffect
import io.soma.cryptobook.core.presentation.UiState
import java.math.BigDecimal

data class HomeUiState(
    val coins: List<CoinItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val realtimeStatusMessage: String? = null,
) : UiState

data class CoinItem(
    val symbol: String,
    val imageUrl: String,
    val price: BigDecimal,
    val priceChangePercentage24h: Double,
)

sealed interface HomeEvent : Event {
    data object OnRefresh : HomeEvent
    data object OnBackClicked : HomeEvent
    data class OnCoinClicked(val symbol: String) : HomeEvent
}

sealed interface HomeSideEffect : SideEffect

fun CoinPriceVO.toCoinItem(imageUrl: String) = CoinItem(
    symbol = symbol,
    imageUrl = imageUrl,
    price = price,
    priceChangePercentage24h = priceChangePercentage24h,
)
