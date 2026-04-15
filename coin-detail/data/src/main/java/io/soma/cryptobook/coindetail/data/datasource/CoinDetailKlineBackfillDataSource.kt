package io.soma.cryptobook.coindetail.data.datasource

import io.soma.cryptobook.coindetail.data.network.BinanceSpotKlineClient
import io.soma.cryptobook.core.data.model.CoinKlineDto
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import javax.inject.Inject

class CoinDetailKlineBackfillDataSource @Inject constructor(
    private val klineClient: BinanceSpotKlineClient,
) {
    private companion object {
        private const val EARLIEST_START_TIME_MS = 0L
        private const val MAX_PAGE_SIZE = 1000
    }

    suspend fun getKlines(
        symbol: String,
        interval: String,
        limit: Int = 120,
        startTime: Long? = null,
        endTime: Long? = null,
    ): List<CoinKlineDto> {
        val normalizedSymbol = symbol.uppercase()
        val normalizedInterval = interval.lowercase()
        val rows = klineClient.getKlines(
            symbol = normalizedSymbol,
            interval = normalizedInterval,
            startTime = startTime,
            endTime = endTime,
            limit = limit,
        )
        return rows.mapNotNull { row ->
            row.toCoinKlineDto(
                symbol = normalizedSymbol,
                interval = normalizedInterval,
            )
        }
    }

    suspend fun getAllKlines(
        symbol: String,
        interval: String,
        pageLimit: Int = MAX_PAGE_SIZE,
    ): List<CoinKlineDto> {
        val normalizedSymbol = symbol.uppercase()
        val normalizedInterval = interval.lowercase()
        val allCandles = mutableListOf<CoinKlineDto>()
        var nextStartTime = EARLIEST_START_TIME_MS

        while (true) {
            val rows = klineClient.getKlines(
                symbol = normalizedSymbol,
                interval = normalizedInterval,
                startTime = nextStartTime,
                endTime = null,
                limit = pageLimit,
            )
            if (rows.isEmpty()) break

            val candles = rows.mapNotNull { row ->
                row.toCoinKlineDto(
                    symbol = normalizedSymbol,
                    interval = normalizedInterval,
                )
            }
            if (candles.isEmpty()) break

            allCandles += candles

            if (rows.size < pageLimit) break

            val candidateStartTime = candles.last().openTime + 1
            if (candidateStartTime <= nextStartTime) break
            nextStartTime = candidateStartTime
        }

        return allCandles
            .sortedBy(CoinKlineDto::openTime)
            .distinctBy(CoinKlineDto::openTime)
    }

    private fun List<JsonElement>.toCoinKlineDto(symbol: String, interval: String): CoinKlineDto? {
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
