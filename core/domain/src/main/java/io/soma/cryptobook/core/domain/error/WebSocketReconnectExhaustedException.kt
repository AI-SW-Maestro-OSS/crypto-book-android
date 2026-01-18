package io.soma.cryptobook.core.domain.error

class WebSocketReconnectExhaustedException : Exception(
    "WebSocket reconnection attempts exhausted",
)
