package io.soma.cryptobook.core.domain.usecase

import kotlinx.coroutines.flow.Flow

interface ObserveMarketRealtimeState {
    operator fun invoke(): Flow<MarketRealtimeState>
}

sealed interface MarketRealtimeState {
    data object Inactive : MarketRealtimeState
    data object Connecting : MarketRealtimeState
    data object Connected : MarketRealtimeState
    data object Recovering : MarketRealtimeState

    data class Failed(
        val cause: Throwable,
        val occurredAtMillis: Long,
    ) : MarketRealtimeState
}
