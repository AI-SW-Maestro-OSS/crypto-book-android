package io.soma.cryptobook.core.data.realtime.market

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface MarketRealtimeCoordinator {
    val runtimeState: StateFlow<MarketRealtimeRuntimeState>
    val demandFatalSignals: SharedFlow<MarketRealtimeDemandFatalSignal>

    fun start()
    fun stop()

    suspend fun retainSymbol(symbol: String)
    suspend fun releaseSymbol(symbol: String)
}

data class MarketRealtimeRuntimeState(
    val isStarted: Boolean,
    val isConnected: Boolean,
    val isRecovering: Boolean,
    val lastFatalError: Throwable?,
    val lastUpdatedAtMillis: Long,
) {
    companion object {
        fun initial(nowMillis: Long) = MarketRealtimeRuntimeState(
            isStarted = false,
            isConnected = false,
            isRecovering = false,
            lastFatalError = null,
            lastUpdatedAtMillis = nowMillis,
        )
    }
}

sealed interface MarketRealtimeDemandFatalSignal {
    data class Overview(val cause: Throwable) : MarketRealtimeDemandFatalSignal
    data class Symbol(val symbol: String, val cause: Throwable) : MarketRealtimeDemandFatalSignal
}
