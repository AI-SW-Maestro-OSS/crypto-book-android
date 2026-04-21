package io.soma.cryptobook.core.domain.model

import java.math.BigDecimal

data class CoinPriceVO(
    val symbol: String,
    val price: BigDecimal,
    val priceChangePercentage24h: Double,
    val tickSize: BigDecimal? = null,
)
