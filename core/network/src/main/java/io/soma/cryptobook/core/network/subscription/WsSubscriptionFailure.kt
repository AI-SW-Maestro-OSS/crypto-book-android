package io.soma.cryptobook.core.network.subscription

data class WsSubscriptionFailure(
    val method: WsSubscriptionMethod,
    val streams: Set<String>,
    val attempt: Int,
    val cause: Throwable,
)
