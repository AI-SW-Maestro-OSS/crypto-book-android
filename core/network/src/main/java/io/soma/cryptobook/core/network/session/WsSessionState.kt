package io.soma.cryptobook.core.network.session

sealed interface WsSessionState {
    data object Idle : WsSessionState
    data class Connecting(val attempt: Int) : WsSessionState
    data class Connected(val connectedAtMs: Long) : WsSessionState
    data class Rotating(val reason: RotateReason) : WsSessionState
    data class Reconnecting(val attempt: Int, val delayMs: Long, val cause: Throwable?) :
        WsSessionState

    data class Exhausted(val attempt: Int, val cause: Throwable?) : WsSessionState
    data object Stopped : WsSessionState
}

enum class RotateReason {
    MaxLifetime,
    Manual,
}
