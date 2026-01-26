package io.soma.cryptobook.coindetail.presentation

import io.soma.cryptobook.core.presentation.Event
import io.soma.cryptobook.core.presentation.SideEffect
import io.soma.cryptobook.core.presentation.UiState

data class CoinDetailUiState(
    val symbol: String = "",
    val currentPrice: String = "",
    val priceChangeText: String = "",
    val priceChangePercent: Double = 0.0,
    val high24h: String = "",
    val low24h: String = "",
    val volume24h: String = "",
    val openPrice: String = "",
    val isLoading: Boolean = true,
    val errorMsg: String? = null,
) : UiState

sealed interface CoinDetailEvent : Event {
    data object OnBackClicked : CoinDetailEvent
}

sealed interface CoinDetailSideEffect : SideEffect
