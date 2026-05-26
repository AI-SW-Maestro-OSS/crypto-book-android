package io.soma.cryptobook.core.network.subscription

import io.soma.cryptobook.core.network.BinanceWebSocketClient
import io.soma.cryptobook.core.network.session.WsSessionManager
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject

class DefaultWsSubscriptionManager @Inject constructor(
    private val sessionManager: WsSessionManager,
    private val transport: WsControlTransport,
    private val scope: CoroutineScope,
    private val policy: WsSubscriptionPolicy,
) : WsSubscriptionManager {

    private val requestIdCounter = atomic(1)
    private val mutex = Mutex()
    private val commandMutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    private val desiredRefCount = LinkedHashMap<String, Int>()
    private val confirmed = LinkedHashSet<String>()
    private val pendingById = LinkedHashMap<Int, PendingRequest>()

    private val _failures = MutableSharedFlow<WsSubscriptionFailure>(extraBufferCapacity = 64)
    override val failures: SharedFlow<WsSubscriptionFailure> = _failures.asSharedFlow()

    private var reconcileJob: Job? = null

    init {
        scope.launch {
            sessionManager.events.collect(::handleSessionEvent)
        }
    }

    override suspend fun retain(streams: Set<String>) {
        val normalized = normalizeStreams(streams)
        if (normalized.isEmpty()) return

        commandMutex.withLock {
            val subscribeDelta = mutex.withLock {
                val delta = LinkedHashSet<String>()
                normalized.forEach { stream ->
                    val previous = desiredRefCount[stream] ?: 0
                    desiredRefCount[stream] = previous + 1
                    if (previous == 0 && stream !in confirmed) delta.add(stream)
                }
                delta
            }
            if (subscribeDelta.isEmpty()) return@withLock
            if (!sessionManager.isConnected) return@withLock
            sendWithRetry(WsSubscriptionMethod.Subscribe, subscribeDelta)
        }
    }

    override suspend fun release(streams: Set<String>) {
        val normalized = normalizeStreams(streams)
        if (normalized.isEmpty()) return

        commandMutex.withLock {
            val unsubscribeDelta = mutex.withLock {
                val delta = LinkedHashSet<String>()
                normalized.forEach { stream ->
                    val previous = desiredRefCount[stream] ?: 0
                    when {
                        previous <= 0 -> Unit

                        previous == 1 -> {
                            desiredRefCount.remove(stream)
                            if (stream in confirmed) delta.add(stream)
                        }

                        else -> desiredRefCount[stream] = previous - 1
                    }
                }
                delta
            }
            if (unsubscribeDelta.isEmpty()) return@withLock
            if (!sessionManager.isConnected) return@withLock
            sendWithRetry(WsSubscriptionMethod.Unsubscribe, unsubscribeDelta)
        }
    }

    private fun handleSessionEvent(event: BinanceWebSocketClient.Event) {
        when (event) {
            is BinanceWebSocketClient.Event.Connected -> scheduleReconcile()

            is BinanceWebSocketClient.Event.Message -> {
                scope.launch { handleControlMessage(event.message) }
            }

            is BinanceWebSocketClient.Event.Disconnected,
            is BinanceWebSocketClient.Event.Error,
            -> scope.launch {
                mutex.withLock {
                    confirmed.clear()
                    pendingById.clear()
                }
            }
        }
    }

    private fun scheduleReconcile() {
        reconcileJob?.cancel()
        reconcileJob = scope.launch {
            commandMutex.withLock { reconcileWithServer() }
        }
    }

    private suspend fun reconcileWithServer() {
        if (!sessionManager.isConnected) return

        val listAck = sendWithRetry(WsSubscriptionMethod.ListSubscriptions, emptySet())
        val serverSet = (listAck as? RequestAck.ListSuccess)?.streams ?: return

        val desiredSet = mutex.withLock { desiredRefCount.keys.toSet() }
        val toSubscribe = desiredSet - serverSet
        val toUnsubscribe = serverSet - desiredSet

        if (toSubscribe.isNotEmpty()) {
            sendWithRetry(WsSubscriptionMethod.Subscribe, toSubscribe)
        }
        if (toUnsubscribe.isNotEmpty()) {
            sendWithRetry(WsSubscriptionMethod.Unsubscribe, toUnsubscribe)
        }
    }

    private suspend fun sendWithRetry(
        method: WsSubscriptionMethod,
        streams: Set<String>,
    ): RequestAck {
        if (method != WsSubscriptionMethod.ListSubscriptions && streams.isEmpty()) {
            return RequestAck.Success
        }

        var lastCause: Throwable = IllegalStateException("Subscription command failed")
        for (attempt in 1..policy.maxRequestRetry) {
            val id = requestIdCounter.getAndIncrement()
            when (val ack = sendAndAwait(method, streams, id)) {
                is RequestAck.Success -> {
                    applySuccess(method, streams)
                    return ack
                }

                is RequestAck.ListSuccess -> {
                    applyListResult(ack.streams)
                    return ack
                }

                is RequestAck.Failure -> lastCause = ack.cause
            }
            if (attempt < policy.maxRequestRetry) delay(policy.backoffForAttempt(attempt))
        }

        _failures.tryEmit(
            WsSubscriptionFailure(
                method = method,
                streams = streams,
                attempt = policy.maxRequestRetry,
                cause = lastCause,
            ),
        )
        return RequestAck.Failure(lastCause)
    }

    private suspend fun sendAndAwait(
        method: WsSubscriptionMethod,
        streams: Set<String>,
        id: Int,
    ): RequestAck {
        val deferred = CompletableDeferred<RequestAck>()
        val pending = PendingRequest(id, method, streams, deferred)
        mutex.withLock { pendingById[id] = pending }

        val sent = transport.sendCommand(
            method = method.wireValue,
            params = streams.sorted().toList(),
            id = id,
        )
        if (!sent) {
            mutex.withLock { pendingById.remove(id) }
            return RequestAck.Failure(
                IllegalStateException("Failed to send ${method.wireValue}: $streams"),
            )
        }

        val timeoutJob = scope.launch {
            delay(policy.requestTimeoutMs)
            deferred.complete(RequestAck.Failure(AckTimeoutException(method, id)))
        }

        return try {
            deferred.await()
        } finally {
            timeoutJob.cancel()
            mutex.withLock { pendingById.remove(id) }
        }
    }

    private suspend fun handleControlMessage(message: String) {
        val control = parseControlMessage(message) ?: return
        val pending = mutex.withLock { pendingById[control.id] } ?: return

        when (control) {
            is ControlMessage.Error -> pending.deferred.complete(
                RequestAck.Failure(
                    AckErrorException(pending.method, control.id, control.code, control.msg),
                ),
            )

            is ControlMessage.Result -> {
                if (pending.method == WsSubscriptionMethod.ListSubscriptions) {
                    pending.deferred.complete(
                        RequestAck.ListSuccess(parseStreamsFromResult(control.result)),
                    )
                } else {
                    pending.deferred.complete(RequestAck.Success)
                }
            }
        }
    }

    private suspend fun applySuccess(method: WsSubscriptionMethod, streams: Set<String>) {
        mutex.withLock {
            when (method) {
                WsSubscriptionMethod.Subscribe -> confirmed.addAll(streams)
                WsSubscriptionMethod.Unsubscribe -> confirmed.removeAll(streams)
                WsSubscriptionMethod.ListSubscriptions -> Unit
            }
        }
    }

    private suspend fun applyListResult(streams: Set<String>) {
        mutex.withLock {
            confirmed.clear()
            confirmed.addAll(streams)
        }
    }

    private fun parseControlMessage(message: String): ControlMessage? {
        val obj = runCatching { json.parseToJsonElement(message).jsonObject }.getOrNull()
            ?: return null
        val id = (obj["id"] as? JsonPrimitive)?.intOrNull ?: return null
        val code = (obj["code"] as? JsonPrimitive)?.intOrNull
        val msg = (obj["msg"] as? JsonPrimitive)?.contentOrNull
        if (code != null || msg != null) return ControlMessage.Error(id, code, msg)
        if (!obj.containsKey("result")) return null
        return ControlMessage.Result(id, obj.getValue("result"))
    }

    private fun parseStreamsFromResult(result: JsonElement): Set<String> {
        val array = result as? JsonArray ?: return emptySet()
        return array.mapNotNull { element ->
            (element as? JsonPrimitive)?.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }
        }.toSet()
    }

    private fun normalizeStreams(streams: Set<String>): Set<String> = streams
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()

    private data class PendingRequest(
        val id: Int,
        val method: WsSubscriptionMethod,
        val streams: Set<String>,
        val deferred: CompletableDeferred<RequestAck>,
    )

    private sealed interface RequestAck {
        data object Success : RequestAck
        data class ListSuccess(val streams: Set<String>) : RequestAck
        data class Failure(val cause: Throwable) : RequestAck
    }

    private sealed interface ControlMessage {
        val id: Int
        data class Result(override val id: Int, val result: JsonElement) : ControlMessage
        data class Error(override val id: Int, val code: Int?, val msg: String?) : ControlMessage
    }

    private class AckTimeoutException(method: WsSubscriptionMethod, id: Int) :
        Exception("ACK timeout for ${method.wireValue} (id=$id)")

    private class AckErrorException(
        method: WsSubscriptionMethod,
        id: Int,
        code: Int?,
        msg: String?,
    ) : Exception("ACK error for ${method.wireValue} (id=$id, code=$code, msg=$msg)")
}
