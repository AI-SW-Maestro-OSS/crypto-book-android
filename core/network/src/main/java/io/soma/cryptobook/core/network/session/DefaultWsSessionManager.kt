package io.soma.cryptobook.core.network.session

import io.soma.cryptobook.core.domain.error.WebSocketReconnectExhaustedException
import io.soma.cryptobook.core.network.BinanceWebSocketClient
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow
import kotlin.random.Random

class DefaultWsSessionManager @Inject constructor(
    private val transport: BinanceWebSocketClient,
    private val scope: CoroutineScope,
    private val policy: WsSessionPolicy,
) : WsSessionManager {

    private val consumerCount = atomic(0)
    private val reconnectAttempt = atomic(0)
    private val lastInboundAtMs = atomic(0L)

    private val _state = MutableStateFlow<WsSessionState>(WsSessionState.Idle)
    override val state: StateFlow<WsSessionState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<BinanceWebSocketClient.Event>(extraBufferCapacity = 128)
    override val events: SharedFlow<BinanceWebSocketClient.Event> = _events.asSharedFlow()

    override val isConnected: Boolean get() = transport.isConnected

    private var collectorJob: Job? = null
    private var rotateJob: Job? = null
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null

    init {
        collectorJob = scope.launch {
            transport.events.collect { event ->
                handleTransportEvent(event)
            }
        }
    }

    @Synchronized
    override fun acquire() {
        val count = consumerCount.incrementAndGet()
        if (count == 1) {
            reconnectAttempt.value = 0
        }
        ensureConnected()
    }

    @Synchronized
    override fun release() {
        val remain = decrementConsumerCount()
        if (remain == 0) {
            stop()
        }
    }

    @Synchronized
    override fun stop() {
        consumerCount.value = 0
        reconnectAttempt.value = 0
        reconnectJob?.cancel()
        cancelSessionTimers()
        _state.value = WsSessionState.Stopped
        transport.disconnect()
    }

    override fun subscribe(streams: List<String>) {
        transport.subscribe(streams)
    }

    override fun unsubscribe(streams: List<String>) {
        transport.unsubscribe(streams)
    }

    private fun handleTransportEvent(event: BinanceWebSocketClient.Event) {
        when (event) {
            is BinanceWebSocketClient.Event.Connected -> {
                reconnectAttempt.value = 0
                val now = System.currentTimeMillis()
                _state.value = WsSessionState.Connected(connectedAtMs = now)
                scheduleLifetimeRotation()
            }

            is BinanceWebSocketClient.Event.Message -> {
            }

            is BinanceWebSocketClient.Event.Disconnected -> {
                cancelSessionTimers()
                if (consumerCount.value == 0) {
                    _state.value = WsSessionState.Stopped
                } else {
                    scheduleReconnect(cause = null)
                }
            }

            is BinanceWebSocketClient.Event.Error -> {
                cancelSessionTimers()
                if (consumerCount.value == 0) {
                    _state.value = WsSessionState.Stopped
                } else {
                    scheduleReconnect(cause = event.throwable)
                }
            }
        }

        _events.tryEmit(event)
    }

    @Synchronized
    private fun ensureConnected() {
        if (consumerCount.value <= 0) return
        if (transport.isConnected) return
        if (_state.value is WsSessionState.Connecting) return
        if (_state.value is WsSessionState.Reconnecting) return

        val attempt = (reconnectAttempt.value + 1).coerceAtLeast(1)
        _state.value = WsSessionState.Connecting(attempt = attempt)
        transport.connect()
    }

    private fun scheduleLifetimeRotation() {
        rotateJob?.cancel()
        val rotateInMs = (policy.maxLifetimeMs - policy.rotateBeforeExpiryMs).coerceAtLeast(0L)

        rotateJob = scope.launch {
            delay(rotateInMs)
            if (consumerCount.value <= 0) return@launch
            if (!transport.isConnected) return@launch

            _state.value = WsSessionState.Rotating(RotateReason.MaxLifetime)
            transport.disconnect()
        }
    }

    @Synchronized
    private fun scheduleReconnect(cause: Throwable?) {
        if (consumerCount.value <= 0) return

        val attempt = reconnectAttempt.incrementAndGet()
        if (attempt > policy.maxReconnectCount) {
            _state.value = WsSessionState.Exhausted(attempt = attempt, cause = cause)
            _events.tryEmit(
                BinanceWebSocketClient.Event.Error(WebSocketReconnectExhaustedException()),
            )
            return
        }

        val delayMs = computeBackoffDelay(attempt)
        _state.value = WsSessionState.Reconnecting(
            attempt = attempt,
            delayMs = delayMs,
            cause = cause,
        )

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(delayMs)
            if (consumerCount.value <= 0) return@launch
            _state.value = WsSessionState.Connecting(attempt = attempt)
            transport.connect()
        }
    }

    private fun computeBackoffDelay(attempt: Int): Long {
        val exp = (
            policy.initialReconnectDelayMs *
                policy.backoffMultiplier.pow((attempt - 1).toDouble())
            ).toLong().coerceAtMost(policy.maxReconnectDelayMs)

        if (policy.jitterRatio <= 0.0) return exp

        val jitterBound = (exp * policy.jitterRatio).toLong()
        if (jitterBound <= 0L) return exp

        val jitter = Random.nextLong(from = -jitterBound, until = jitterBound + 1)
        return (exp + jitter).coerceAtLeast(0L)
    }

    private fun cancelSessionTimers() {
        rotateJob?.cancel()
    }

    private fun decrementConsumerCount(): Int {
        while (true) {
            val current = consumerCount.value
            if (current == 0) return 0
            val next = current - 1
            if (consumerCount.compareAndSet(current, next)) return next
        }
    }
}
