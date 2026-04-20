package io.soma.cryptobook.core.data.realtime.market

import io.soma.cryptobook.core.data.realtime.kline.InMemoryWsKlineTable
import io.soma.cryptobook.core.data.realtime.ticker.InMemoryWsTickerTable
import io.soma.cryptobook.core.domain.error.WebSocketReconnectExhaustedException
import io.soma.cryptobook.core.network.BinanceWebSocketClient
import io.soma.cryptobook.core.network.market.WsMarketMessage
import io.soma.cryptobook.core.network.market.WsMarketMessageRouter
import io.soma.cryptobook.core.network.market.WsMarketStreamEvent
import io.soma.cryptobook.core.network.session.WsSessionManager
import io.soma.cryptobook.core.network.session.WsSessionState
import io.soma.cryptobook.core.network.subscription.WsSubscriptionFailure
import io.soma.cryptobook.core.network.subscription.WsSubscriptionManager
import io.soma.cryptobook.core.network.subscription.WsSubscriptionSnapshot
import io.soma.cryptobook.core.network.subscription.WsSubscriptionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultMarketRealtimeCoordinatorTest {
    private val dispatcher = StandardTestDispatcher()

    @Test
    fun `start and stop hold app session lease once`() = runTest(dispatcher) {
        val sessionManager = FakeWsSessionManager()
        val subscriptionManager = FakeWsSubscriptionManager()
        val coordinator = createCoordinator(
            scope = backgroundScope,
            sessionManager = sessionManager,
            subscriptionManager = subscriptionManager,
        )

        coordinator.start()
        coordinator.start()
        runCurrent()
        coordinator.stop()
        coordinator.stop()
        runCurrent()

        assertEquals(1, sessionManager.acquireCalls)
        assertEquals(1, sessionManager.releaseCalls)
        assertEquals(listOf(setOf("!miniTicker@arr")), subscriptionManager.retainCalls)
        assertEquals(listOf(setOf("!miniTicker@arr")), subscriptionManager.releaseCalls)
        assertFalse(coordinator.runtimeState.value.isStarted)
    }

    @Test
    fun `start then stop keeps overview baseline retain release ordered`() = runTest(dispatcher) {
        val subscriptionManager = FakeWsSubscriptionManager()
        val coordinator = createCoordinator(
            scope = backgroundScope,
            subscriptionManager = subscriptionManager,
        )

        coordinator.start()
        coordinator.stop()
        runCurrent()

        assertEquals(
            listOf(
                "retain:[!miniTicker@arr]",
                "release:[!miniTicker@arr]",
            ),
            subscriptionManager.operations,
        )
    }

    @Test
    fun `symbol demand retains and releases ticker plus kline streams`() = runTest(dispatcher) {
        val subscriptionManager = FakeWsSubscriptionManager()
        val coordinator = createCoordinator(
            scope = backgroundScope,
            subscriptionManager = subscriptionManager,
        )

        coordinator.retainSymbol("btcusdt")
        coordinator.retainSymbol("BTCUSDT")
        coordinator.releaseSymbol("BTCUSDT")
        coordinator.releaseSymbol("BTCUSDT")

        assertEquals(
            listOf(setOf("btcusdt@ticker", "btcusdt@kline_1d")),
            subscriptionManager.retainCalls,
        )
        assertEquals(
            listOf(setOf("btcusdt@ticker", "btcusdt@kline_1d")),
            subscriptionManager.releaseCalls,
        )
    }

    @Test
    fun `fatal runtime error clears when session transitions back to healthy state`() = runTest(
        dispatcher,
    ) {
        val sessionManager = FakeWsSessionManager()
        val coordinator = createCoordinator(
            scope = backgroundScope,
            sessionManager = sessionManager,
        )

        coordinator.start()
        runCurrent()
        sessionManager.state.value = WsSessionState.Exhausted(
            attempt = 1,
            cause = WebSocketReconnectExhaustedException(),
        )
        runCurrent()

        assertTrue(
            coordinator.runtimeState.value.lastFatalError is WebSocketReconnectExhaustedException,
        )

        sessionManager.state.value = WsSessionState.Reconnecting(
            attempt = 1,
            delayMs = 1000L,
            cause = null,
        )
        runCurrent()

        assertNull(coordinator.runtimeState.value.lastFatalError)
        assertTrue(coordinator.runtimeState.value.isRecovering)
    }

    private fun createCoordinator(
        scope: CoroutineScope,
        sessionManager: FakeWsSessionManager = FakeWsSessionManager(),
        subscriptionManager: FakeWsSubscriptionManager = FakeWsSubscriptionManager(),
        router: FakeWsMarketMessageRouter = FakeWsMarketMessageRouter(),
        tickerTable: InMemoryWsTickerTable = InMemoryWsTickerTable(),
        klineTable: InMemoryWsKlineTable = InMemoryWsKlineTable(),
    ): DefaultMarketRealtimeCoordinator = DefaultMarketRealtimeCoordinator(
        sessionManager = sessionManager,
        subscriptionManager = subscriptionManager,
        marketMessageRouter = router,
        tickerTable = tickerTable,
        klineTable = klineTable,
        payloadMapper = MarketRealtimePayloadMapper(),
        scope = scope,
    )

    private class FakeWsSessionManager : WsSessionManager {
        override val state = MutableStateFlow<WsSessionState>(WsSessionState.Idle)
        private val _events = MutableSharedFlow<BinanceWebSocketClient.Event>(
            replay = 16,
            extraBufferCapacity = 16,
        )
        override val events: SharedFlow<BinanceWebSocketClient.Event> = _events
        override val isConnected: Boolean get() = state.value is WsSessionState.Connected

        var acquireCalls = 0
        var releaseCalls = 0

        override fun acquire() {
            acquireCalls += 1
        }

        override fun release() {
            releaseCalls += 1
        }

        override fun stop() = Unit

        override fun subscribe(streams: List<String>) = Unit

        override fun unsubscribe(streams: List<String>) = Unit
    }

    private class FakeWsSubscriptionManager : WsSubscriptionManager {
        override val state: StateFlow<WsSubscriptionState> =
            MutableStateFlow(WsSubscriptionState())
        private val _failures = MutableSharedFlow<WsSubscriptionFailure>(
            replay = 16,
            extraBufferCapacity = 16,
        )
        override val failures: SharedFlow<WsSubscriptionFailure> = _failures

        val retainCalls = mutableListOf<Set<String>>()
        val releaseCalls = mutableListOf<Set<String>>()
        val operations = mutableListOf<String>()

        override suspend fun retain(streams: Set<String>) {
            retainCalls += streams
            operations += "retain:$streams"
        }

        override suspend fun release(streams: Set<String>) {
            releaseCalls += streams
            operations += "release:$streams"
        }

        override fun snapshot(): WsSubscriptionSnapshot = WsSubscriptionSnapshot(
            desiredRefCount = emptyMap(),
            desiredStreams = emptySet(),
            confirmedStreams = emptySet(),
            pendingRequestIds = emptySet(),
        )
    }

    private class FakeWsMarketMessageRouter : WsMarketMessageRouter {
        private val _streamEvents = MutableSharedFlow<WsMarketStreamEvent>(
            replay = 16,
            extraBufferCapacity = 16,
        )
        private val _events = MutableSharedFlow<WsMarketMessage>(
            replay = 16,
            extraBufferCapacity = 16,
        )

        override val streamEvents: SharedFlow<WsMarketStreamEvent> = _streamEvents
        override val events: SharedFlow<WsMarketMessage> = _events

        fun emit(message: WsMarketMessage) {
            _events.tryEmit(message)
        }
    }
}
