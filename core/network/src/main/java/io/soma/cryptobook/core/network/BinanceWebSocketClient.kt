package io.soma.cryptobook.core.network

import android.util.Log
import io.soma.cryptobook.core.network.subscription.WsControlTransport
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
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

class BinanceWebSocketClient(private val client: OkHttpClient, scope: CoroutineScope) :
    WsControlTransport {
    sealed interface Event {
        data class Message(val message: String) : Event
        data class Error(val throwable: Throwable) : Event
        data object Connected : Event
        data object Disconnected : Event
    }

    private val requestIdCounter = atomic(1)

    private val isConnectedRef = atomic(false)
    val isConnected: Boolean get() = isConnectedRef.value
    override val isSocketReady: Boolean get() = isConnected

    /**
     * Listener thread receives raw events here without ever blocking.
     * A single dispatcher coroutine forwards them to [_events] in order, suspending
     * when the SharedFlow buffer is full instead of dropping messages.
     */
    private val inbox = Channel<Event>(capacity = Channel.UNLIMITED)

    private val _events = MutableSharedFlow<Event>(
        extraBufferCapacity = 256,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )
    val events = _events.asSharedFlow()

    init {
        scope.launch {
            for (event in inbox) {
                _events.emit(event)
            }
        }
    }

    companion object {
        private const val TAG = "BinanceWS"
        private const val BASE_URL = "wss://stream.binance.com:9443/ws"
    }

    private var webSocket: WebSocket? = null

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "Connected")
            isConnectedRef.value = true
            inbox.trySend(Event.Connected)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            inbox.trySend(Event.Message(text))
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "Failed: ${t.message}")
            isConnectedRef.value = false
            inbox.trySend(Event.Error(t))
            this@BinanceWebSocketClient.webSocket = null
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "Closed: code=$code reason=$reason")
            isConnectedRef.value = false
            inbox.trySend(Event.Disconnected)
            this@BinanceWebSocketClient.webSocket = null
        }
    }

    fun connect() {
        if (webSocket != null) return
        Log.d(TAG, "Connecting...")
        val request = Request.Builder().url(BASE_URL).build()
        webSocket = client.newWebSocket(request, listener)
    }

    fun disconnect() {
        isConnectedRef.value = false
        webSocket?.close(1000, "Normal closure")
        webSocket = null
    }

    fun subscribe(streams: List<String>) {
        if (!isConnected) return
        val id = requestIdCounter.getAndIncrement()
        sendCommand("SUBSCRIBE", streams, id)
    }

    fun unsubscribe(streams: List<String>) {
        if (!isConnected) return
        val id = requestIdCounter.getAndIncrement()
        sendCommand("UNSUBSCRIBE", streams, id)
    }

    override fun sendCommand(method: String, params: List<Any>, id: Int): Boolean {
        if (!isConnected) return false
        val json = createCommandJson(method = method, params = params, id = id)
        return webSocket?.send(json) == true
    }

    private fun createCommandJson(method: String, params: List<Any>, id: Int): String =
        JSONObject().apply {
            put("method", method)
            if (params.isNotEmpty()) {
                put("params", JSONArray(params))
            }
            put("id", id)
        }.toString()
}
