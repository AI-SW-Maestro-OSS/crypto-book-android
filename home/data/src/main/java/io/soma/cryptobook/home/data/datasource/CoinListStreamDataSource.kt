package io.soma.cryptobook.home.data.datasource

import io.soma.cryptobook.core.data.model.CoinTickerDto
import io.soma.cryptobook.core.network.BinanceWebSocketClient
import io.soma.cryptobook.core.network.session.WsSessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import javax.inject.Inject

class CoinListStreamDataSource @Inject constructor(
    private val sessionManager: WsSessionManager,
    private val json: Json,
) {
    sealed interface State {
        data class Success(val tickers: List<CoinTickerDto>) : State
        data class Error(val throwable: Throwable) : State
        data object Connected : State
        data object Disconnected : State
    }

    private val targetStream = "!ticker@arr"

    fun observeCoinList(): Flow<State> = flow {
        sessionManager.acquire()

        if (sessionManager.isConnected) {
            sessionManager.subscribe(listOf(targetStream))
            emit(State.Connected)
        }

        try {
            sessionManager.events.collect { event ->
                when (event) {
                    is BinanceWebSocketClient.Event.Message -> {
                        val isTargetEvent = event.message.trim()
                            .startsWith("[") && event.message.contains("24hrTicker")
                        if (isTargetEvent) {
                            try {
                                val tickers =
                                    json.decodeFromString<List<CoinTickerDto>>(event.message)
                                emit(State.Success(tickers))
                            } catch (e: Exception) {
                            }
                        }
                    }

                    is BinanceWebSocketClient.Event.Connected -> {
                        sessionManager.subscribe(listOf(targetStream))
                        emit(State.Connected)
                    }

                    is BinanceWebSocketClient.Event.Disconnected -> {
                        emit(State.Disconnected)
                    }

                    is BinanceWebSocketClient.Event.Error -> {
                        emit(State.Error(event.throwable))
                    }
                }
            }
        } finally {
            sessionManager.unsubscribe(listOf(targetStream))
            sessionManager.release()
        }
    }
}
