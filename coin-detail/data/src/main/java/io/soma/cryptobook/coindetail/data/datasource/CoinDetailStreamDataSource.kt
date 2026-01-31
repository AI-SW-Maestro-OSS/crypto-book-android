package io.soma.cryptobook.coindetail.data.datasource

import io.soma.cryptobook.core.data.model.CoinTickerDto
import io.soma.cryptobook.core.network.BinanceConnectionManager
import io.soma.cryptobook.core.network.SubscriptionManager
import io.soma.cryptobook.core.network.table.WebSocketTableManager
import io.soma.cryptobook.core.network.table.WebSocketTableManager.TableState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json
import javax.inject.Inject

class CoinDetailStreamDataSource @Inject constructor(
    private val tableManager: WebSocketTableManager,
    private val subscriptionManager: SubscriptionManager,
    private val connectionManager: BinanceConnectionManager,
    private val json: Json,
) {
    sealed interface State {
        data object Loading : State
        data class Success(val ticker: CoinTickerDto) : State
        data class Error(val throwable: Throwable) : State
    }

    fun observeCoinDetail(symbol: String): Flow<State> {
        val streamName = "${symbol.lowercase()}@ticker"

        return tableManager
            .getTable(streamName)
            .map { tableState ->
                when (tableState) {
                    is TableState.Empty -> State.Loading
                    is TableState.Data -> {
                        val ticker = json.decodeFromString<CoinTickerDto>(tableState.rawJson)
                        State.Success(ticker)
                    }
                }
            }
            .catch { e -> emit(State.Error(e)) }
            .onStart {
                connectionManager.connect()
                subscriptionManager.subscribe(streamName)
            }
            .onCompletion {
                subscriptionManager.unsubscribe(streamName)
            }
    }
}