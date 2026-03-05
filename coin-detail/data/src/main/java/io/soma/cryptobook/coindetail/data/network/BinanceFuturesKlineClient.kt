package io.soma.cryptobook.coindetail.data.network

import kotlinx.serialization.json.JsonElement

interface BinanceFuturesKlineClient {
    suspend fun getKlines(
        symbol: String,
        interval: String,
        limit: Int,
    ): List<List<JsonElement>>
}
