package io.soma.cryptobook.coindetail.data.model

import io.soma.cryptobook.core.data.model.CoinTickerDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BinanceFuturesTickerDto(
    @SerialName("symbol")
    val symbol: String,
    @SerialName("lastPrice")
    val lastPrice: String,
    @SerialName("priceChangePercent")
    val priceChangePercent: String,
    @SerialName("priceChange")
    val priceChange: String,
    @SerialName("lowPrice")
    val lowPrice: String,
    @SerialName("highPrice")
    val highPrice: String,
    @SerialName("quoteVolume")
    val quoteVolume: String,
    @SerialName("openPrice")
    val openPrice: String,
)

fun BinanceFuturesTickerDto.toCoinTickerDto(): CoinTickerDto = CoinTickerDto(
    symbol = symbol.uppercase(),
    lastPrice = lastPrice,
    priceChangePercent = priceChangePercent,
    priceChange = priceChange,
    lowPrice = lowPrice,
    highPrice = highPrice,
    quoteAssetVolume = quoteVolume,
    openPrice = openPrice,
)
