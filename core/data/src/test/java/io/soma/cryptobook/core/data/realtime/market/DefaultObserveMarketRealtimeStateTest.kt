package io.soma.cryptobook.core.data.realtime.market

import io.soma.cryptobook.core.domain.usecase.ObserveMarketRealtimeState
import io.soma.cryptobook.core.domain.usecase.MarketRealtimeState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultObserveMarketRealtimeStateTest {
    @Test
    fun `maps inactive state`() = runTest {
        val coordinator = FakeMarketRealtimeCoordinator()
        val observer = DefaultObserveMarketRealtimeState(coordinator)

        val result = observer().first()

        assertEquals(MarketRealtimeState.Inactive, result)
    }

    @Test
    fun `maps failed state when coordinator has fatal error`() = runTest {
        val coordinator = FakeMarketRealtimeCoordinator()
        val error = IllegalStateException("boom")
        coordinator.runtimeStateFlow.value = MarketRealtimeRuntimeState(
            isStarted = true,
            isConnected = false,
            isRecovering = false,
            lastFatalError = error,
            lastUpdatedAtMillis = 12L,
        )
        val observer = DefaultObserveMarketRealtimeState(coordinator)

        val result = observer().first()

        assertTrue(result is MarketRealtimeState.Failed)
        result as MarketRealtimeState.Failed
        assertEquals(error, result.cause)
        assertEquals(12L, result.occurredAtMillis)
    }

    @Test
    fun `maps recovering state`() = runTest {
        val coordinator = FakeMarketRealtimeCoordinator()
        coordinator.runtimeStateFlow.value = MarketRealtimeRuntimeState(
            isStarted = true,
            isConnected = false,
            isRecovering = true,
            lastFatalError = null,
            lastUpdatedAtMillis = 7L,
        )
        val observer = DefaultObserveMarketRealtimeState(coordinator)

        val result = observer().first()

        assertEquals(MarketRealtimeState.Recovering, result)
    }

    private class FakeMarketRealtimeCoordinator : MarketRealtimeCoordinator {
        val runtimeStateFlow = MutableStateFlow(MarketRealtimeRuntimeState.initial(nowMillis = 0L))
        override val runtimeState: StateFlow<MarketRealtimeRuntimeState> = runtimeStateFlow
        override val demandFatalSignals: SharedFlow<MarketRealtimeDemandFatalSignal> =
            MutableSharedFlow()

        override fun start() = Unit

        override fun stop() = Unit

        override suspend fun retainSymbol(symbol: String) = Unit

        override suspend fun releaseSymbol(symbol: String) = Unit
    }
}
