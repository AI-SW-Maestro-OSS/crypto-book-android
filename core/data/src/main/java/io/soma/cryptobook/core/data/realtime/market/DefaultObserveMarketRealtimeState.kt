package io.soma.cryptobook.core.data.realtime.market

import io.soma.cryptobook.core.domain.usecase.MarketRealtimeState
import io.soma.cryptobook.core.domain.usecase.ObserveMarketRealtimeState
import io.soma.cryptobook.core.network.session.WsSessionManager
import io.soma.cryptobook.core.network.session.WsSessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class DefaultObserveMarketRealtimeState @Inject constructor(
    private val coordinator: MarketRealtimeCoordinator,
    private val sessionManager: WsSessionManager,
) : ObserveMarketRealtimeState {
    override fun invoke(): Flow<MarketRealtimeState> = combine(
        coordinator.isStarted,
        sessionManager.state,
    ) { started, sessionState ->
        when {
            !started -> MarketRealtimeState.Inactive

            sessionState is WsSessionState.Exhausted -> MarketRealtimeState.Failed(
                cause = sessionState.cause ?: IllegalStateException("ws session exhausted"),
                occurredAtMillis = System.currentTimeMillis(),
            )

            sessionState is WsSessionState.Connected -> MarketRealtimeState.Connected

            sessionState is WsSessionState.Reconnecting ||
                sessionState is WsSessionState.Rotating -> MarketRealtimeState.Recovering

            else -> MarketRealtimeState.Connecting
        }
    }.distinctUntilChanged()
}
