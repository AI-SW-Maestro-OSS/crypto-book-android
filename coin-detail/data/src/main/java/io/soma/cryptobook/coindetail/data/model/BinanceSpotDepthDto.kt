package io.soma.cryptobook.coindetail.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Binance spot REST depth snapshot (`GET /api/v3/depth`).
 *
 * `bids`/`asks` are arrays of `[price, quantity]` string pairs.
 */
@Serializable
data class BinanceSpotDepthDto(
    @SerialName("lastUpdateId") val lastUpdateId: Long,
    @SerialName("bids") val bids: List<List<String>> = emptyList(),
    @SerialName("asks") val asks: List<List<String>> = emptyList(),
)
