package io.soma.cryptobook.core.network.subscription

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface WsSubscriptionManager {
    val state: StateFlow<WsSubscriptionState>
    val failures: SharedFlow<WsSubscriptionFailure>

    suspend fun retain(streams: Set<String>)
    suspend fun release(streams: Set<String>)
    fun snapshot(): WsSubscriptionSnapshot
}
