package io.soma.cryptobook.core.network.stream

import io.soma.cryptobook.core.network.market.WsMarketMessage
import io.soma.cryptobook.core.network.market.WsMarketMessageRouter
import io.soma.cryptobook.core.network.session.WsSessionManager
import io.soma.cryptobook.core.network.subscription.WsSubscriptionFailure
import io.soma.cryptobook.core.network.subscription.WsSubscriptionManager
import io.soma.cryptobook.core.network.subscription.WsSubscriptionMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class DefaultWsStreamSource(
    private val sessionManager: WsSessionManager,
    private val subscriptionManager: WsSubscriptionManager,
    private val router: WsMarketMessageRouter,
    private val scope: CoroutineScope,
) : WsStreamSource {

    private val cacheMutex = Mutex()
    private val sharedByKey = HashMap<Set<String>, SharedFlow<WsMarketMessage>>()

    override fun subscribe(streams: Set<String>): Flow<WsMarketMessage> {
        require(streams.isNotEmpty()) { "streams must not be empty" }
        val key = streams.toSortedSet().toSet()
        return flow {
            val upstream = cacheMutex.withLock {
                sharedByKey.getOrPut(key) { createUpstream(key) }
            }
            coroutineScope {
                val failureWatcher = launch {
                    subscriptionManager.failures
                        .filter { it.affects(key) }
                        .collect { failure -> throw failure.cause }
                }
                try {
                    emitAll(upstream)
                } finally {
                    failureWatcher.cancel()
                }
            }
        }
    }

    private fun createUpstream(streams: Set<String>): SharedFlow<WsMarketMessage> = router.events
        .filter { it.matches(streams) }
        .onStart {
            sessionManager.acquire()
            subscriptionManager.retain(streams)
        }
        .onCompletion {
            withContext(NonCancellable) {
                subscriptionManager.release(streams)
                sessionManager.release()
            }
        }
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(WsStreamSource.STOP_TIMEOUT_MS),
            replay = 0,
        )

    private fun WsMarketMessage.matches(streams: Set<String>): Boolean = when (this) {
        is WsMarketMessage.AllMiniTickers -> OVERVIEW_STREAM in streams

        is WsMarketMessage.SymbolTicker -> "${ticker.symbol.lowercase()}@ticker" in streams

        is WsMarketMessage.SymbolKline ->
            "${klineEvent.symbol.lowercase()}@kline_${klineEvent.kline.interval.lowercase()}" in
                streams

        is WsMarketMessage.SymbolDepth -> {
            val prefix = "${depthEvent.symbol.lowercase()}@depth"
            streams.any { it.startsWith(prefix) }
        }

        WsMarketMessage.Ignored -> false
    }

    private fun WsSubscriptionFailure.affects(streams: Set<String>): Boolean =
        method == WsSubscriptionMethod.ListSubscriptions ||
            this.streams.any { it in streams }

    private companion object {
        private const val OVERVIEW_STREAM = "!miniTicker@arr"
    }
}
