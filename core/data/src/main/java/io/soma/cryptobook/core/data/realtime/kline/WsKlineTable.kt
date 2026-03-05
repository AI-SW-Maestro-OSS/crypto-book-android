package io.soma.cryptobook.core.data.realtime.kline

import io.soma.cryptobook.core.data.model.CoinKlineDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

data class KlineKey(
    val symbol: String,
    val interval: String,
)

interface WsKlineTable {
    val table: StateFlow<Map<KlineKey, List<CoinKlineDto>>>

    fun upsert(symbol: String, interval: String, candle: CoinKlineDto)
    fun replace(symbol: String, interval: String, candles: List<CoinKlineDto>)
    fun observe(symbol: String, interval: String): Flow<List<CoinKlineDto>>
    fun clear()
}
