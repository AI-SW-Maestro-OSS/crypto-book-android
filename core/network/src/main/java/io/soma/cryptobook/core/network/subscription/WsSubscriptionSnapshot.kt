package io.soma.cryptobook.core.network.subscription

data class WsSubscriptionSnapshot(
    val desiredRefCount: Map<String, Int>,
    val desiredStreams: Set<String>,
    val confirmedStreams: Set<String>,
    val pendingRequestIds: Set<Int>,
)
