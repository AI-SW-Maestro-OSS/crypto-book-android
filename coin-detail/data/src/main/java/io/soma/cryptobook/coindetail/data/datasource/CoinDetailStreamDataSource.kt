package io.soma.cryptobook.coindetail.data.datasource

import io.soma.cryptobook.core.data.model.CoinTickerDto
import io.soma.cryptobook.core.data.realtime.ticker.WsTickerTable
import io.soma.cryptobook.core.domain.error.WebSocketReconnectExhaustedException
import io.soma.cryptobook.core.network.BinanceWebSocketClient
import io.soma.cryptobook.core.network.session.WsSessionManager
import io.soma.cryptobook.core.network.subscription.WsSubscriptionFailure
import io.soma.cryptobook.core.network.subscription.WsSubscriptionManager
import io.soma.cryptobook.core.network.subscription.WsSubscriptionMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.serialization.json.Json
import javax.inject.Inject

class CoinDetailStreamDataSource @Inject constructor(
    private val sessionManager: WsSessionManager,
    private val subscriptionManager: WsSubscriptionManager,
    private val tickerTable: WsTickerTable,
    private val json: Json,
) {
    sealed interface State {
        data class Success(val ticker: CoinTickerDto) : State
        data class Error(val throwable: Throwable) : State
        data object Connected : State
        data object Disconnected : State
    }

    fun observeCoinDetail(symbol: String): Flow<State> = flow {
        val targetStream = "${symbol.lowercase()}@ticker"
        val targetSymbol = symbol.uppercase()

        sessionManager.acquire()
        subscriptionManager.retain(setOf(targetStream))

        if (sessionManager.isConnected) {
            emit(State.Connected)
        }

        try {
            merge(
                sessionManager.events.map<BinanceWebSocketClient.Event, StreamEvent> {
                    StreamEvent.Transport(it)
                },
                subscriptionManager.failures.map<WsSubscriptionFailure, StreamEvent> {
                    StreamEvent.SubscriptionFailure(it)
                },
            ).collect { streamEvent ->
                when (streamEvent) {
                    is StreamEvent.Transport -> {
                        when (val event = streamEvent.event) {
                            is BinanceWebSocketClient.Event.Message -> {
                                val isTargetEvent = event.message.trim()
                                    .startsWith("{") && event.message.contains("24hrTicker")
                                if (isTargetEvent) {
                                    try {
                                        val ticker =
                                            json.decodeFromString<CoinTickerDto>(event.message)
                                        // target symbol만 emit
                                        if (ticker.symbol == targetSymbol) {
                                            tickerTable.upsert(ticker)
                                            emit(State.Success(ticker))
                                        }
                                    } catch (e: Exception) { // 파싱 실패 무시
                                    }
                                }
                            }

                            is BinanceWebSocketClient.Event.Connected -> {
                                emit(State.Connected)
                            }

                            is BinanceWebSocketClient.Event.Disconnected -> {
                                tickerTable.clear()
                                emit(State.Disconnected)
                            }

                            is BinanceWebSocketClient.Event.Error -> {
                                if (event.throwable is WebSocketReconnectExhaustedException) {
                                    tickerTable.clear()
                                }
                                emit(State.Error(event.throwable))
                            }
                        }
                    }

                    is StreamEvent.SubscriptionFailure -> {
                        val failure = streamEvent.failure
                        val isGlobalFailure =
                            failure.method == WsSubscriptionMethod.ListSubscriptions
                        val isTargetFailure = targetStream in failure.streams

                        if (isGlobalFailure || isTargetFailure) {
                            if (failure.cause is WebSocketReconnectExhaustedException) {
                                tickerTable.clear()
                            }
                            emit(State.Error(failure.cause))
                        }
                    }
                }
            }
        } finally {
            subscriptionManager.release(setOf(targetStream))
            sessionManager.release()
        }
    }

    private sealed interface StreamEvent {
        data class Transport(val event: BinanceWebSocketClient.Event) : StreamEvent
        data class SubscriptionFailure(val failure: WsSubscriptionFailure) : StreamEvent
    }
}
