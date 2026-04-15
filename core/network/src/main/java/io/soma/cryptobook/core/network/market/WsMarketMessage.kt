package io.soma.cryptobook.core.network.market

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface WsMarketMessage {
    data class AllMiniTickers(val tickers: List<WsMiniTickerPayload>) : WsMarketMessage
    data class SymbolTicker(val ticker: WsTickerPayload) : WsMarketMessage
    data class SymbolKline(val klineEvent: WsKlineEventPayload) : WsMarketMessage
    data object Ignored : WsMarketMessage
}

@Serializable
data class WsTickerPayload(
    @SerialName("e") val eventType: String? = null,
    @SerialName("s") val symbol: String,
    @SerialName("c") val lastPrice: String,
    @SerialName("P") val priceChangePercent: String,
    @SerialName("p") val priceChange: String,
    @SerialName("l") val lowPrice: String,
    @SerialName("h") val highPrice: String,
    @SerialName("q") val quoteAssetVolume: String,
    @SerialName("o") val openPrice: String,
)

@Serializable
data class WsMiniTickerPayload(
    @SerialName("e") val eventType: String? = null,
    @SerialName("s") val symbol: String,
    @SerialName("c") val lastPrice: String,
    @SerialName("o") val openPrice: String,
    @SerialName("h") val highPrice: String,
    @SerialName("l") val lowPrice: String,
    @SerialName("v") val volume: String,
    @SerialName("q") val quoteAssetVolume: String,
)

@Serializable
data class WsKlineEventPayload(
    @SerialName("e") val eventType: String? = null,
    @SerialName("s") val symbol: String,
    @SerialName("k") val kline: WsKlinePayload,
)

@Serializable
data class WsKlinePayload(
    @SerialName("t") val openTime: Long,
    @SerialName("T") val closeTime: Long,
    @SerialName("s") val symbol: String,
    @SerialName("i") val interval: String,
    @SerialName("o") val openPrice: String,
    @SerialName("c") val closePrice: String,
    @SerialName("h") val highPrice: String,
    @SerialName("l") val lowPrice: String,
    @SerialName("v") val volume: String,
    @SerialName("x") val isClosed: Boolean,
)
