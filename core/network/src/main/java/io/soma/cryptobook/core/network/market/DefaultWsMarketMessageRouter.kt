package io.soma.cryptobook.core.network.market

import io.soma.cryptobook.core.network.BinanceWebSocketClient
import io.soma.cryptobook.core.network.session.WsSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class DefaultWsMarketMessageRouter(
    sessionManager: WsSessionManager,
    private val parser: WsMarketMessageParser,
    scope: CoroutineScope,
) : WsMarketMessageRouter {
    private val debugLogger = WsMarketDebugLogger()

    private val _streamEvents = MutableSharedFlow<WsMarketStreamEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val streamEvents: SharedFlow<WsMarketStreamEvent> = _streamEvents.asSharedFlow()

    private val _events = MutableSharedFlow<WsMarketMessage>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val events: SharedFlow<WsMarketMessage> = _events.asSharedFlow()

    init {
        scope.launch {
            sessionManager.events.collect { event ->
                when (event) {
                    is BinanceWebSocketClient.Event.Message -> {
                        val sequence = debugLogger.nextSequence()
                        debugLogger.logRaw(seq = sequence, raw = event.message)
                        val message = parser.parse(event.message)
                        debugLogger.logParsed(seq = sequence, message = message)
                        if (message !is WsMarketMessage.Ignored) {
                            _events.tryEmit(message)
                            _streamEvents.tryEmit(WsMarketStreamEvent.Market(message))
                        }
                    }

                    is BinanceWebSocketClient.Event.Connected,
                    is BinanceWebSocketClient.Event.Disconnected,
                    is BinanceWebSocketClient.Event.Error,
                    -> {
                        _streamEvents.tryEmit(WsMarketStreamEvent.Transport(event))
                    }
                }
            }
        }
    }
}
