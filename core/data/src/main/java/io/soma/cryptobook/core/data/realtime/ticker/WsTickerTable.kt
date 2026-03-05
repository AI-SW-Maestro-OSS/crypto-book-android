package io.soma.cryptobook.core.data.realtime.ticker

import io.soma.cryptobook.core.data.model.CoinTickerDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface WsTickerTable {
    val table: StateFlow<Map<String, CoinTickerDto>>

    fun upsertAll(tickers: List<CoinTickerDto>)
    fun upsert(ticker: CoinTickerDto)
    fun clear()

    fun observeSymbol(symbol: String): Flow<CoinTickerDto?>
}
