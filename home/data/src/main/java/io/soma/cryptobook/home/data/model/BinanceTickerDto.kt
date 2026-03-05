package io.soma.cryptobook.home.data.model

import io.soma.cryptobook.core.domain.model.CoinPriceVO
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BinanceTickerDto(
    @SerialName("symbol")
    val symbol: String,
    @SerialName("lastPrice")
    val lastPrice: String,
    @SerialName("priceChangePercent")
    val priceChangePercent: String,
)

fun BinanceTickerDto.toCoinPriceVO() = CoinPriceVO(
    symbol = symbol,
    price = lastPrice.toBigDecimal(),
    priceChangePercentage24h = priceChangePercent.toDouble(),
)
