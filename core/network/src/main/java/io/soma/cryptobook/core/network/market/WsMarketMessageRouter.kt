package io.soma.cryptobook.core.network.market

import io.soma.cryptobook.core.network.BinanceWebSocketClient
import kotlinx.coroutines.flow.SharedFlow

interface WsMarketMessageRouter {
    val streamEvents: SharedFlow<WsMarketStreamEvent>
    val events: SharedFlow<WsMarketMessage>
}

sealed interface WsMarketStreamEvent {
    data class Transport(val event: BinanceWebSocketClient.Event) : WsMarketStreamEvent
    data class Market(val message: WsMarketMessage) : WsMarketStreamEvent
}
