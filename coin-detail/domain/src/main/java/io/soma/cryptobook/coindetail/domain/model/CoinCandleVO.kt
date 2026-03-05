package io.soma.cryptobook.coindetail.domain.model

data class CoinCandleVO(
    val openTime: Long,
    val closeTime: Long,
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double,
    val volume: Double,
)
