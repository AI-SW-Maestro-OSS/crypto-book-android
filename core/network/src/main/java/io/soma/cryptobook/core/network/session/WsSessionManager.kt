package io.soma.cryptobook.core.network.session

import io.soma.cryptobook.core.network.BinanceWebSocketClient
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface WsSessionManager {
    val state: StateFlow<WsSessionState>
    val events: SharedFlow<BinanceWebSocketClient.Event>
    val isConnected: Boolean

    fun acquire()
    fun release()
    fun stop()

    fun subscribe(streams: List<String>)
    fun unsubscribe(streams: List<String>)
}
