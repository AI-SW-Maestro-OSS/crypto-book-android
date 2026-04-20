package io.soma.cryptobook.core.data.realtime.market

import io.soma.cryptobook.core.domain.usecase.MarketRealtimeState
import io.soma.cryptobook.core.domain.usecase.ObserveMarketRealtimeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultObserveMarketRealtimeState @Inject constructor(
    private val coordinator: MarketRealtimeCoordinator,
) : ObserveMarketRealtimeState {
    override fun invoke(): Flow<MarketRealtimeState> = coordinator.runtimeState
        .map(::toResult)
        .distinctUntilChanged()

    private fun toResult(state: MarketRealtimeRuntimeState): MarketRealtimeState = when {
        !state.isStarted -> MarketRealtimeState.Inactive
        state.lastFatalError != null -> MarketRealtimeState.Failed(
            cause = state.lastFatalError,
            occurredAtMillis = state.lastUpdatedAtMillis,
        )
        state.isConnected -> MarketRealtimeState.Connected
        state.isRecovering -> MarketRealtimeState.Recovering
        else -> MarketRealtimeState.Connecting
    }
}
