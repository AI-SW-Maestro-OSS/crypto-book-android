package io.soma.cryptobook.core.network.table

import io.soma.cryptobook.core.network.BinanceConnectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class WebSocketTableManager @Inject constructor(
    private val connectionManager: BinanceConnectionManager,
    private val scope: CoroutineScope,
) {
    sealed class TableState {
        data object Empty : TableState()
        data class Data(val rawJson: String) : TableState()
    }

    private val tables = ConcurrentHashMap<String, MutableStateFlow<TableState>>()

    init {
        scope.launch {
            connectionManager.events.collect { event ->
                when (event) {
                    is BinanceConnectionManager.Event.Message -> routeMessage(event.message)
                    is BinanceConnectionManager.Event.Disconnected -> clearAllTables()
                    else -> { }
                }
            }
        }
    }

    fun getTable(streamName: String): StateFlow<TableState> {
        return tables.getOrPut(streamName) {
            MutableStateFlow(TableState.Empty)
        }
    }

    private fun routeMessage(message: String) {
        val streamName = extractStreamName(message) ?: return
        tables[streamName]?.value = TableState.Data(message)
    }

    private fun clearAllTables() {
        tables.values.forEach { it.value = TableState.Empty }
    }

    private fun extractStreamName(message: String): String? {
        val trimmed = message.trim()
        return when {
            // 배열 형식: !ticker@arr
            trimmed.startsWith("[") && trimmed.contains("24hrTicker") -> "!ticker@arr"
            // 개별 티커: "s" 필드(Symbol)로 스트림 이름 추론
            trimmed.startsWith("{") && trimmed.contains("\"24hrTicker\"") -> {
                val regex = """"s"\s*:\s*"([^"]+)"""".toRegex()
                regex.find(trimmed)?.groupValues?.get(1)?.lowercase()?.let { "$it@ticker" }
            }
            else -> null
        }
    }
}
