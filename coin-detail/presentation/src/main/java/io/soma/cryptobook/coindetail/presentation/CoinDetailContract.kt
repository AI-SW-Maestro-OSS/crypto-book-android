package io.soma.cryptobook.coindetail.presentation

import io.soma.cryptobook.core.presentation.Event
import io.soma.cryptobook.core.presentation.SideEffect
import io.soma.cryptobook.core.presentation.UiState
import java.math.BigDecimal

data class CoinDetailUiState(
    val symbol: String = "",
    val imageUrl: String = "",
    val currentPrice: String = "",
    val priceChangeText: String = "",
    val priceChangePercent: Double = 0.0,
    val candles: List<CandleUiModel> = emptyList(),
    val high24h: String = "",
    val low24h: String = "",
    val volume24h: String = "",
    val openPrice: String = "",
    val tickSize: BigDecimal? = null,
    val isLoading: Boolean = true,
    val errorMsg: String? = null,
    val realtimeStatusMessage: String? = null,
) : UiState

data class CandleUiModel(
    val openTime: Long,
    val closeTime: Long,
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double,
)

sealed interface CoinDetailEvent : Event {
    data object OnBackClicked : CoinDetailEvent
    data object OnScreenStarted : CoinDetailEvent
}

sealed interface CoinDetailSideEffect : SideEffect
