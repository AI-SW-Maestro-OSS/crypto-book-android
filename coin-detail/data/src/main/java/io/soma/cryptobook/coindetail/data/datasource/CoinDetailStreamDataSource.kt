package io.soma.cryptobook.coindetail.data.datasource

import io.soma.cryptobook.core.data.model.CoinTickerDto
import io.soma.cryptobook.core.network.BinanceWebSocketClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import javax.inject.Inject

class CoinDetailStreamDataSource @Inject constructor(
    private val webSocketClient: BinanceWebSocketClient,
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

        webSocketClient.connect()

        if (webSocketClient.isConnected) {
            webSocketClient.subscribe(listOf(targetStream))
            emit(State.Connected)
        }

        try {
            webSocketClient.events.collect { event ->
                when (event) {
                    is BinanceWebSocketClient.Event.Message -> {
                        val isTargetEvent = event.message.trim()
                            .startsWith("{") && event.message.contains("24hrTicker")
                        if (isTargetEvent) {
                            try {
                                val ticker =
                                    json.decodeFromString<CoinTickerDto>(event.message)
                                // target symbol만 emit
                                if (ticker.symbol == targetSymbol) {
                                    emit(State.Success(ticker))
                                }
                            } catch (e: Exception) { // 파싱 실패 무시
                            }
                        }
                    }

                    is BinanceWebSocketClient.Event.Connected -> {
                        webSocketClient.subscribe(listOf(targetStream))
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
            webSocketClient.unsubscribe(listOf(targetStream))
        }
    }
}
