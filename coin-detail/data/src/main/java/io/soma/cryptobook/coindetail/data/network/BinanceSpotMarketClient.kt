package io.soma.cryptobook.coindetail.data.network

import io.soma.cryptobook.coindetail.data.model.BinanceSpotTickerDto
import kotlinx.serialization.json.JsonElement

interface BinanceSpotKlineClient {
    suspend fun getKlines(
        symbol: String,
        interval: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
    ): List<List<JsonElement>>
}

interface BinanceSpotTickerClient {
    suspend fun getTicker(symbol: String): BinanceSpotTickerDto
}
