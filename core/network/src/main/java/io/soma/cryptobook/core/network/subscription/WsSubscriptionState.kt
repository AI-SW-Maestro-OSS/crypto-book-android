package io.soma.cryptobook.core.network.subscription

data class WsSubscriptionState(
    val desiredStreams: Set<String> = emptySet(),
    val confirmedStreams: Set<String> = emptySet(),
    val pendingRequestIds: Set<Int> = emptySet(),
    val lastFailure: WsSubscriptionFailure? = null,
)
