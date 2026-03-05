package io.soma.cryptobook.coindetail.data.network

import io.soma.cryptobook.coindetail.data.model.BinanceFuturesTickerDto
import kotlinx.serialization.json.JsonElement

interface BinanceFuturesKlineClient {
    suspend fun getKlines(
        symbol: String,
        interval: String,
        limit: Int,
    ): List<List<JsonElement>>
}

interface BinanceFuturesTickerClient {
    suspend fun getTicker(symbol: String): BinanceFuturesTickerDto
}
