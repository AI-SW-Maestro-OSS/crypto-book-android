package io.soma.cryptobook.home.data.datasource

import io.soma.cryptobook.core.data.model.CoinTickerDto
import io.soma.cryptobook.core.network.BinanceWebSocketClient
import io.soma.cryptobook.core.network.SubscriptionManager
import io.soma.cryptobook.core.network.table.WebSocketTableManager
import io.soma.cryptobook.core.network.table.WebSocketTableManager.TableState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json

class CoinListStreamDataSource(
    private val tableManager: WebSocketTableManager,
    private val subscriptionManager: SubscriptionManager,
    private val webSocketClient: BinanceWebSocketClient,
    private val json: Json,
) {
    sealed class State {
        data object Loading : State()
        data class Success(val tickers: List<CoinTickerDto>) : State()
        data class Error(val throwable: Throwable) : State()
    }

    fun observeTickers(): Flow<State> = tableManager
        .getTable(STREAM_NAME)
        .map { tableState ->
            when (tableState) {
                is TableState.Empty -> State.Loading
                is TableState.Data -> {
                    val tickers = json.decodeFromString<List<CoinTickerDto>>(tableState.rawJson)
                    State.Success(tickers)
                }
            }
        }
        .catch { e -> emit(State.Error(e)) }
        .onStart {
            webSocketClient.connect()
            subscriptionManager.subscribe(STREAM_NAME)
        }
        .onCompletion {
            subscriptionManager.unsubscribe(STREAM_NAME)
        }

    companion object {
        private const val STREAM_NAME = "!ticker@arr"
    }
}