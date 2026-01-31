package io.soma.cryptobook.core.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class SubscriptionTable @Inject constructor(
    private val webSocketClient: BinanceWebSocketClient,
    private val scope: CoroutineScope,
) {
    private val counts = ConcurrentHashMap<String, AtomicInteger>()

    init {
        scope.launch {
            webSocketClient.events.collect { event ->
                if (event is BinanceWebSocketClient.Event.Connected) {
                    resubscribeAll()
                }
            }
        }
    }

    fun subscribe(stream: String) {
        val count = counts.getOrPut(stream) { AtomicInteger(0) }
        if (count.incrementAndGet() == 1) {
            Log.d(TAG, "Subscribe: $stream")
            webSocketClient.sendSubscribe(stream)
        }
    }

    fun unsubscribe(stream: String) {
        val count = counts[stream] ?: return
        if (count.decrementAndGet() == 0) {
            counts.remove(stream)
            Log.d(TAG, "Unsubscribe: $stream")
            webSocketClient.sendUnsubscribe(stream)
        }
    }

    private fun resubscribeAll() {
        val streams = counts.keys.toList()
        if (streams.isNotEmpty()) {
            Log.d(TAG, "Resubscribe all: $streams")
            webSocketClient.sendSubscribe(streams)
        }
    }

    companion object {
        private const val TAG = "SubscriptionTable"
    }
}
