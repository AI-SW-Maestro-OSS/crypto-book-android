package io.soma.cryptobook.core.data.model.ticksize

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BinanceExchangeInfoDto(
    @SerialName("symbols")
    val symbols: List<BinanceSymbolInfoDto> = emptyList(),
)

@Serializable
data class BinanceSymbolInfoDto(
    @SerialName("symbol")
    val symbol: String,
    @SerialName("filters")
    val filters: List<BinanceSymbolFilterDto> = emptyList(),
)

@Serializable
data class BinanceSymbolFilterDto(
    @SerialName("filterType")
    val filterType: String,
    @SerialName("tickSize")
    val tickSize: String? = null,
)
