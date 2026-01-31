package io.soma.cryptobook.core.network

import android.util.Log
import io.soma.cryptobook.core.domain.error.WebSocketReconnectExhaustedException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.math.pow

class BinanceWebSocketClient @Inject constructor(
    private val client: OkHttpClient,
    private val scope: CoroutineScope,
) {
    sealed class Event {
        data class Message(val message: String) : Event()
        data class Error(val throwable: Throwable) : Event()
        data object Connected : Event()
        data object Disconnected : Event()
    }

    private val requestIdCounter = AtomicInteger(1)

    private val _isConnected = AtomicBoolean(false)
    val isConnected: Boolean get() = _isConnected.get()
    private val retryCount = AtomicInteger(0)
    private var reconnectJob: Job? = null
    private val intentionalDisconnect = AtomicBoolean(false)

    private val _events = MutableSharedFlow<Event>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events = _events.asSharedFlow()

    companion object {
        private const val TAG = "BinanceWS"
        private const val BASE_URL = "wss://fstream.binance.com/ws"

        // 재연결 상수
        private const val INITIAL_DELAY_MS = 1000L
        private const val MAX_DELAY_MS = 30_000L
        private const val MAX_RETRY_COUNT = 5
        private const val BACKOFF_MULTIPLIER = 2.0
    }

    private var webSocket: WebSocket? = null

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "Connected")
            _isConnected.set(true)
            retryCount.set(0)
            reconnectJob?.cancel()
            _events.tryEmit(Event.Connected)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            _events.tryEmit(Event.Message(text))
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "Failed: ${t.message}")
            _isConnected.set(false)
            _events.tryEmit(Event.Error(t))
            this@BinanceWebSocketClient.webSocket = null
            reconnect()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "Closed: code=$code")
            _isConnected.set(false)
            _events.tryEmit(Event.Disconnected)
            this@BinanceWebSocketClient.webSocket = null
            if (code != 1000 && !intentionalDisconnect.get()) {
                reconnect()
            }
        }
    }

    fun connect() {
        if (webSocket != null) return
        Log.d(TAG, "Connecting... retry=${retryCount.get()}")
        intentionalDisconnect.set(false)
        val request = Request.Builder().url(BASE_URL).build()
        webSocket = client.newWebSocket(request, listener)
    }

    fun disconnect() {
        intentionalDisconnect.set(true)
        reconnectJob?.cancel()
        retryCount.set(0)
        _isConnected.set(false)
        webSocket?.close(1000, "Normal closure")
        webSocket = null
    }

    fun sendSubscribe(stream: String) {
        sendSubscribe(listOf(stream))
    }

    fun sendSubscribe(streams: List<String>) {
        if (!isConnected) return
        val id = requestIdCounter.getAndIncrement()
        val json = createSubscriptionJson("SUBSCRIBE", streams, id)
        webSocket?.send(json)
    }

    fun sendUnsubscribe(stream: String) {
        sendUnsubscribe(listOf(stream))
    }

    fun sendUnsubscribe(streams: List<String>) {
        if (!isConnected) return
        val id = requestIdCounter.getAndIncrement()
        val json = createSubscriptionJson("UNSUBSCRIBE", streams, id)
        webSocket?.send(json)
    }

    private fun createSubscriptionJson(method: String, params: List<String>, id: Int): String =
        JSONObject().apply {
            put("method", method)
            put("params", JSONArray(params))
            put("id", id)
        }.toString()

    private fun reconnect() {
        if (intentionalDisconnect.get()) return

        val currentRetry = retryCount.incrementAndGet()
        if (currentRetry > MAX_RETRY_COUNT) {
            Log.w(TAG, "Reconnect gave up after $MAX_RETRY_COUNT retries")
            _events.tryEmit(Event.Error(WebSocketReconnectExhaustedException()))
            return
        }

        val delayMs = (INITIAL_DELAY_MS * BACKOFF_MULTIPLIER.pow(currentRetry - 1))
            .toLong().coerceAtMost(MAX_DELAY_MS)
        Log.d(TAG, "Reconnecting #$currentRetry in ${delayMs}ms")

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(delayMs)
            if (!intentionalDisconnect.get()) {
                connect()
            }
        }
    }
}
