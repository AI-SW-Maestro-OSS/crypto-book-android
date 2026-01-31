package io.soma.cryptobook.home.data.datasource

import io.soma.cryptobook.core.data.model.CoinTickerDto
import io.soma.cryptobook.core.network.BinanceWebSocketClient
import io.soma.cryptobook.core.network.SubscriptionTable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class CoinListStreamDataSource(
    private val webSocketClient: BinanceWebSocketClient,
    private val subscriptionTable: SubscriptionTable,
    private val json: Json,
    private val scope: CoroutineScope,
) {
    sealed class State {
        data class Success(val tickers: List<CoinTickerDto>) : State()
        data class Error(val throwable: Throwable) : State()
        data object Connected : State()
        data object Disconnected : State()
        data object Idle : State()
    }

    private val tickerState = MutableStateFlow<State>(State.Idle)

    init {
        scope.launch {
            webSocketClient.events.collect { event ->
                when (event) {
                    is BinanceWebSocketClient.Event.Message -> {
                        val isTargetEvent = event.message.trim()
                            .startsWith("[") && event.message.contains("24hrTicker")
                        if (isTargetEvent) {
                            try {
                                val tickers =
                                    json.decodeFromString<List<CoinTickerDto>>(event.message)
                                tickerState.value = State.Success(tickers)
                            } catch (e: Exception) {
                                // parsing error ignored
                            }
                        }
                    }

                    is BinanceWebSocketClient.Event.Connected -> {
                        tickerState.value = State.Connected
                    }

                    is BinanceWebSocketClient.Event.Disconnected -> {
                        tickerState.value = State.Disconnected
                    }

                    is BinanceWebSocketClient.Event.Error -> {
                        tickerState.value = State.Error(event.throwable)
                    }
                }
            }
        }
    }

    fun observeTickers(): Flow<State> = tickerState
        .onSubscription {
            webSocketClient.connect()
            subscriptionTable.subscribe(STREAM_NAME)
        }
        .onCompletion {
            subscriptionTable.unsubscribe(STREAM_NAME)
        }

    companion object {
        private const val STREAM_NAME = "!ticker@arr"
    }
}
