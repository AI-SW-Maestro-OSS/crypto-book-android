package io.soma.cryptobook.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CoinKlineDto(
    val symbol: String,
    val interval: String,
    val openTime: Long,
    val closeTime: Long,
    val openPrice: String,
    val closePrice: String,
    val highPrice: String,
    val lowPrice: String,
    val volume: String,
    val isClosed: Boolean,
)
