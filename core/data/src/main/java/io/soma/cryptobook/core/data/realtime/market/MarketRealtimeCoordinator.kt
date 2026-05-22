package io.soma.cryptobook.core.data.realtime.market

import kotlinx.coroutines.flow.StateFlow

interface MarketRealtimeCoordinator {
    val isStarted: StateFlow<Boolean>

    fun start()
    fun stop()
}
