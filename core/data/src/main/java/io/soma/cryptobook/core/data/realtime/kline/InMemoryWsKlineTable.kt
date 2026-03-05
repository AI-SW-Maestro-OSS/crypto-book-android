package io.soma.cryptobook.core.data.realtime.kline

import io.soma.cryptobook.core.data.model.CoinKlineDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryWsKlineTable @Inject constructor() : WsKlineTable {
    private companion object {
        private const val MAX_CANDLES = 120
    }

    private val _table = MutableStateFlow<Map<KlineKey, List<CoinKlineDto>>>(emptyMap())
    override val table: StateFlow<Map<KlineKey, List<CoinKlineDto>>> = _table

    override fun upsert(symbol: String, interval: String, candle: CoinKlineDto) {
        val key = normalizeKey(symbol = symbol, interval = interval)
        val normalized = candle.copy(symbol = key.symbol, interval = key.interval)
        _table.update { old ->
            val current = old[key].orEmpty().toMutableList()
            val index = current.indexOfFirst { it.openTime == normalized.openTime }
            if (index >= 0) {
                current[index] = normalized
            } else {
                current.add(normalized)
            }

            val updated = current
                .sortedBy { it.openTime }
                .takeLast(MAX_CANDLES)

            LinkedHashMap(old).apply {
                this[key] = updated
            }
        }
    }

    override fun replace(symbol: String, interval: String, candles: List<CoinKlineDto>) {
        val key = normalizeKey(symbol = symbol, interval = interval)
        val normalized = candles
            .map { it.copy(symbol = key.symbol, interval = key.interval) }
            .sortedBy { it.openTime }
            .takeLast(MAX_CANDLES)
        _table.update { old ->
            LinkedHashMap(old).apply {
                this[key] = normalized
            }
        }
    }

    override fun observe(symbol: String, interval: String): Flow<List<CoinKlineDto>> {
        val key = normalizeKey(symbol = symbol, interval = interval)
        return table.map { it[key].orEmpty() }.distinctUntilChanged()
    }

    override fun clear() {
        _table.value = emptyMap()
    }

    private fun normalizeKey(symbol: String, interval: String): KlineKey = KlineKey(
        symbol = symbol.uppercase(),
        interval = interval.lowercase(),
    )
}
