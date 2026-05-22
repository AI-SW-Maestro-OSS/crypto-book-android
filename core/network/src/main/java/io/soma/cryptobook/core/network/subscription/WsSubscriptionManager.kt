package io.soma.cryptobook.core.network.subscription

import kotlinx.coroutines.flow.SharedFlow

interface WsSubscriptionManager {
    val failures: SharedFlow<WsSubscriptionFailure>

    suspend fun retain(streams: Set<String>)
    suspend fun release(streams: Set<String>)
}
