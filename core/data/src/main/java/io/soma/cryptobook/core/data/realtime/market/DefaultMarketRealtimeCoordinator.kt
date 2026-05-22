package io.soma.cryptobook.core.data.realtime.market

import io.soma.cryptobook.core.data.realtime.ticker.WsTickerTable
import io.soma.cryptobook.core.network.market.WsMarketMessage
import io.soma.cryptobook.core.network.stream.WsStreamSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DefaultMarketRealtimeCoordinator(
    private val wsStreamSource: WsStreamSource,
    private val tickerTable: WsTickerTable,
    private val payloadMapper: MarketRealtimePayloadMapper,
    private val scope: CoroutineScope,
) : MarketRealtimeCoordinator {

    private val _isStarted = MutableStateFlow(false)
    override val isStarted: StateFlow<Boolean> = _isStarted.asStateFlow()

    private var overviewJob: Job? = null

    override fun start() {
        if (overviewJob?.isActive == true) return
        _isStarted.value = true
        overviewJob = scope.launch {
            wsStreamSource.subscribe(setOf(OVERVIEW_STREAM)).collect { message ->
                if (message is WsMarketMessage.AllMiniTickers) {
                    tickerTable.upsertAll(message.tickers.map(payloadMapper::toTickerDto))
                }
            }
        }
    }

    override fun stop() {
        overviewJob?.cancel()
        overviewJob = null
        _isStarted.value = false
    }

    private companion object {
        private const val OVERVIEW_STREAM = "!miniTicker@arr"
    }
}
