package io.soma.cryptobook.core.network.market

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface WsMarketMessage {
    data class AllTickers(val tickers: List<WsTickerPayload>) : WsMarketMessage
    data class SymbolTicker(val ticker: WsTickerPayload) : WsMarketMessage
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
