package io.soma.cryptobook.coindetail.data.datasource

import io.soma.cryptobook.coindetail.data.network.BinanceFuturesKlineClient
import io.soma.cryptobook.core.data.model.CoinKlineDto
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import javax.inject.Inject

class CoinDetailKlineBackfillDataSource @Inject constructor(
    private val klineClient: BinanceFuturesKlineClient,
) {
    suspend fun getKlines(
        symbol: String,
        interval: String,
        limit: Int = 120,
    ): List<CoinKlineDto> {
        val normalizedSymbol = symbol.uppercase()
        val normalizedInterval = interval.lowercase()
        val rows = klineClient.getKlines(
            symbol = normalizedSymbol,
            interval = normalizedInterval,
            limit = limit,
        )
        return rows.mapNotNull { row ->
            row.toCoinKlineDto(
                symbol = normalizedSymbol,
                interval = normalizedInterval,
            )
        }
    }

    private fun List<JsonElement>.toCoinKlineDto(
        symbol: String,
        interval: String,
    ): CoinKlineDto? {
        if (size < 7) return null
        val openTime = this[0].jsonPrimitive.longOrNull ?: return null
        val closeTime = this[6].jsonPrimitive.longOrNull ?: return null
        return CoinKlineDto(
            symbol = symbol,
            interval = interval,
            openTime = openTime,
            closeTime = closeTime,
            openPrice = this[1].jsonPrimitive.contentOrNull ?: return null,
            highPrice = this[2].jsonPrimitive.contentOrNull ?: return null,
            lowPrice = this[3].jsonPrimitive.contentOrNull ?: return null,
            closePrice = this[4].jsonPrimitive.contentOrNull ?: return null,
            volume = this[5].jsonPrimitive.contentOrNull ?: return null,
            isClosed = true,
        )
    }
}
