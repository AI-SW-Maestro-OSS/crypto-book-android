package io.soma.cryptobook.core.data.realtime.ticker

import io.soma.cryptobook.core.data.model.CoinTickerDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryWsTickerTable @Inject constructor() : WsTickerTable {
    private val _table = MutableStateFlow<Map<String, CoinTickerDto>>(emptyMap())
    override val table: StateFlow<Map<String, CoinTickerDto>> = _table

    override fun upsertAll(tickers: List<CoinTickerDto>) {
        if (tickers.isEmpty()) return
        _table.update { old ->
            LinkedHashMap(old).apply {
                tickers.forEach { ticker ->
                    this[ticker.symbol] = ticker
                }
            }
        }
    }

    override fun upsert(ticker: CoinTickerDto) {
        _table.update { old ->
            LinkedHashMap(old).apply {
                this[ticker.symbol] = ticker
            }
        }
    }

    override fun clear() {
        _table.value = emptyMap()
    }

    override fun observeSymbol(symbol: String): Flow<CoinTickerDto?> {
        val targetSymbol = symbol.uppercase()
        return table.map { it[targetSymbol] }.distinctUntilChanged()
    }
}
