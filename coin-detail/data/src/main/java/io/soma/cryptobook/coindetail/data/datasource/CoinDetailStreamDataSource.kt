package io.soma.cryptobook.coindetail.data.datasource

import android.util.Log
import io.soma.cryptobook.core.data.model.CoinKlineDto
import io.soma.cryptobook.core.data.model.CoinTickerDto
import io.soma.cryptobook.core.data.realtime.kline.WsKlineTable
import io.soma.cryptobook.core.data.realtime.ticker.WsTickerTable
import io.soma.cryptobook.core.domain.error.WebSocketReconnectExhaustedException
import io.soma.cryptobook.core.network.BinanceWebSocketClient
import io.soma.cryptobook.core.network.market.WsKlineEventPayload
import io.soma.cryptobook.core.network.market.WsMarketMessage
import io.soma.cryptobook.core.network.market.WsMarketMessageRouter
import io.soma.cryptobook.core.network.market.WsMarketStreamEvent
import io.soma.cryptobook.core.network.market.WsTickerPayload
import io.soma.cryptobook.core.network.session.WsSessionManager
import io.soma.cryptobook.core.network.subscription.WsSubscriptionFailure
import io.soma.cryptobook.core.network.subscription.WsSubscriptionManager
import io.soma.cryptobook.core.network.subscription.WsSubscriptionMethod
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CoinDetailStreamDataSource @Inject constructor(
    private val sessionManager: WsSessionManager,
    private val subscriptionManager: WsSubscriptionManager,
    private val tickerTable: WsTickerTable,
    private val klineTable: WsKlineTable,
    private val tickerSnapshotDataSource: CoinDetailTickerSnapshotDataSource,
    private val klineBackfillDataSource: CoinDetailKlineBackfillDataSource,
    private val marketMessageRouter: WsMarketMessageRouter,
) {
    private companion object {
        private const val TAG = "CoinDetailStream"
        private const val TARGET_INTERVAL = "1d"
        private const val KLINE_BACKFILL_PAGE_LIMIT = 1000
    }

    // Coin detail owns the lifecycle of symbol-specific streams for now.
    fun maintainCoinDetailStream(symbol: String): Flow<Throwable> = channelFlow {
        val tickerStream = "${symbol.lowercase()}@ticker"
        val klineStream = "${symbol.lowercase()}@kline_$TARGET_INTERVAL"
        val targetStreams = setOf(tickerStream, klineStream)
        val targetSymbol = symbol.uppercase()
        var snapshotJob: Job? = null
        var backfillJob: Job? = null

        fun refreshRestData() {
            snapshotJob?.cancel()
            backfillJob?.cancel()

            snapshotJob = launch {
                runCatching {
                    tickerSnapshotDataSource.getTicker(targetSymbol)
                }.onSuccess { ticker ->
                    tickerTable.upsert(ticker)
                }.onFailure { throwable ->
                    Log.d(TAG, "Ticker snapshot failed: ${throwable.message}")
                }
            }

            backfillJob = launch {
                runCatching {
                    klineBackfillDataSource.getAllKlines(
                        symbol = targetSymbol,
                        interval = TARGET_INTERVAL,
                        pageLimit = KLINE_BACKFILL_PAGE_LIMIT,
                    )
                }.onSuccess { candles ->
                    if (candles.isNotEmpty()) {
                        klineTable.replace(
                            symbol = targetSymbol,
                            interval = TARGET_INTERVAL,
                            candles = candles,
                        )
                    }
                }.onFailure { throwable ->
                    Log.d(TAG, "Kline backfill failed: ${throwable.message}")
                }
            }
        }

        sessionManager.acquire()
        try {
            launch {
                merge(
                    marketMessageRouter.streamEvents.map<WsMarketStreamEvent, StreamEvent> {
                        StreamEvent.Router(it)
                    },
                    subscriptionManager.failures.map<WsSubscriptionFailure, StreamEvent> {
                        StreamEvent.SubscriptionFailure(it)
                    },
                ).collect { streamEvent ->
                    when (streamEvent) {
                        is StreamEvent.Router -> {
                            when (val event = streamEvent.event) {
                                is WsMarketStreamEvent.Market -> {
                                    val message = event.message
                                    if (message is WsMarketMessage.SymbolTicker) {
                                        val ticker = message.ticker.toCoinTickerDto()
                                        if (ticker.symbol == targetSymbol) {
                                            tickerTable.upsert(ticker)
                                        }
                                    }

                                    if (message is WsMarketMessage.SymbolKline) {
                                        val candle = message.klineEvent.toCoinKlineDto()
                                        if (
                                            candle.symbol == targetSymbol &&
                                            candle.interval == TARGET_INTERVAL
                                        ) {
                                            klineTable.upsert(
                                                symbol = targetSymbol,
                                                interval = TARGET_INTERVAL,
                                                candle = candle,
                                            )
                                        }
                                    }
                                }

                                is WsMarketStreamEvent.Transport -> {
                                    when (val transportEvent = event.event) {
                                        is BinanceWebSocketClient.Event.Connected -> {
                                            refreshRestData()
                                        }

                                        is BinanceWebSocketClient.Event.Disconnected -> Unit

                                        is BinanceWebSocketClient.Event.Error -> {
                                            val reconnectExhausted =
                                                transportEvent.throwable is
                                                    WebSocketReconnectExhaustedException
                                            if (reconnectExhausted) {
                                                trySend(transportEvent.throwable)
                                            }
                                        }

                                        is BinanceWebSocketClient.Event.Message -> Unit
                                    }
                                }
                            }
                        }

                        is StreamEvent.SubscriptionFailure -> {
                            val failure = streamEvent.failure
                            val isGlobalFailure =
                                failure.method == WsSubscriptionMethod.ListSubscriptions
                            val isTargetFailure = failure.streams.any { it in targetStreams }

                            if (isGlobalFailure || isTargetFailure) {
                                if (failure.cause is WebSocketReconnectExhaustedException) {
                                    trySend(failure.cause)
                                }
                            }
                        }
                    }
                }
            }

            subscriptionManager.retain(targetStreams)

            if (sessionManager.isConnected) {
                refreshRestData()
            }

            awaitCancellation()
        } finally {
            snapshotJob?.cancel()
            backfillJob?.cancel()
            withContext(NonCancellable) {
                subscriptionManager.release(targetStreams)
            }
            sessionManager.release()
        }
    }

    private sealed interface StreamEvent {
        data class Router(val event: WsMarketStreamEvent) : StreamEvent
        data class SubscriptionFailure(val failure: WsSubscriptionFailure) : StreamEvent
    }

    private fun WsTickerPayload.toCoinTickerDto(): CoinTickerDto = CoinTickerDto(
        symbol = symbol,
        lastPrice = lastPrice,
        priceChangePercent = priceChangePercent,
        priceChange = priceChange,
        lowPrice = lowPrice,
        highPrice = highPrice,
        quoteAssetVolume = quoteAssetVolume,
        openPrice = openPrice,
    )

    private fun WsKlineEventPayload.toCoinKlineDto(): CoinKlineDto = CoinKlineDto(
        symbol = symbol.uppercase(),
        interval = kline.interval.lowercase(),
        openTime = kline.openTime,
        closeTime = kline.closeTime,
        openPrice = kline.openPrice,
        closePrice = kline.closePrice,
        highPrice = kline.highPrice,
        lowPrice = kline.lowPrice,
        volume = kline.volume,
        isClosed = kline.isClosed,
    )
}
