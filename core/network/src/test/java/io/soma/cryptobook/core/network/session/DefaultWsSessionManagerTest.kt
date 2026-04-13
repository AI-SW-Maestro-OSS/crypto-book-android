package io.soma.cryptobook.core.network.session

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.soma.cryptobook.core.network.BinanceWebSocketClient
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultWsSessionManagerTest {

    @Test
    fun `acquire called concurrently connects only once`() = runTest {
        val transportEvents = MutableSharedFlow<BinanceWebSocketClient.Event>(extraBufferCapacity = 16)
        val connectCalls = AtomicInteger(0)
        val transport = mockTransport(
            events = transportEvents,
            onConnect = { connectCalls.incrementAndGet() },
        )
        val manager = createManager(
            transport = transport,
            scope = backgroundScope,
        )

        runConcurrently(times = 8) {
            manager.acquire()
        }

        assertEquals(1, connectCalls.get())
        assertEquals(WsSessionState.Connecting(attempt = 1), manager.state.value)
        verify(exactly = 1) { transport.connect() }
    }

    @Test
    fun `release called concurrently disconnects only once when consumers drain to zero`() = runTest {
        val transportEvents = MutableSharedFlow<BinanceWebSocketClient.Event>(extraBufferCapacity = 16)
        val disconnectCalls = AtomicInteger(0)
        val transport = mockTransport(
            events = transportEvents,
            onDisconnect = { disconnectCalls.incrementAndGet() },
        )
        val manager = createManager(
            transport = transport,
            scope = backgroundScope,
        )

        repeat(8) {
            manager.acquire()
        }

        runConcurrently(times = 8) {
            manager.release()
        }

        assertEquals(1, disconnectCalls.get())
        assertEquals(WsSessionState.Stopped, manager.state.value)
        verify(exactly = 1) { transport.disconnect() }
    }

    private fun createManager(
        transport: BinanceWebSocketClient,
        scope: CoroutineScope,
    ): DefaultWsSessionManager = DefaultWsSessionManager(
        transport = transport,
        scope = scope,
        policy = WsSessionPolicy(
            initialReconnectDelayMs = 1_000L,
            maxReconnectDelayMs = 30_000L,
            maxReconnectCount = 5,
            backoffMultiplier = 2.0,
            jitterRatio = 0.0,
        ),
    )

    private fun mockTransport(
        events: MutableSharedFlow<BinanceWebSocketClient.Event>,
        onConnect: () -> Unit = {},
        onDisconnect: () -> Unit = {},
    ): BinanceWebSocketClient {
        val transport = mockk<BinanceWebSocketClient>()
        every { transport.events } returns events
        every { transport.isConnected } returns false
        every { transport.connect() } answers {
            onConnect()
            Unit
        }
        every { transport.disconnect() } answers {
            onDisconnect()
            Unit
        }
        every { transport.subscribe(any()) } just runs
        every { transport.unsubscribe(any()) } just runs
        return transport
    }

    private fun runConcurrently(
        times: Int,
        action: () -> Unit,
    ) {
        val executor = Executors.newFixedThreadPool(times)
        val ready = CountDownLatch(times)
        val start = CountDownLatch(1)
        val done = CountDownLatch(times)

        repeat(times) {
            executor.execute {
                ready.countDown()
                start.await()
                try {
                    action()
                } finally {
                    done.countDown()
                }
            }
        }

        assertTrue(ready.await(3, TimeUnit.SECONDS))
        start.countDown()
        assertTrue(done.await(3, TimeUnit.SECONDS))
        executor.shutdownNow()
    }
}
