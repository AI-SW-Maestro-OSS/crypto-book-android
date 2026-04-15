package io.soma.cryptobook.home.data.datasource

import io.soma.cryptobook.core.data.model.CoinTickerDto
import io.soma.cryptobook.core.data.realtime.ticker.WsTickerTable
import io.soma.cryptobook.core.domain.error.WebSocketReconnectExhaustedException
import io.soma.cryptobook.core.network.BinanceWebSocketClient
import io.soma.cryptobook.core.network.market.WsMarketMessage
import io.soma.cryptobook.core.network.market.WsMarketMessageRouter
import io.soma.cryptobook.core.network.market.WsMarketStreamEvent
import io.soma.cryptobook.core.network.market.WsMiniTickerPayload
import io.soma.cryptobook.core.network.session.WsSessionManager
import io.soma.cryptobook.core.network.subscription.WsSubscriptionFailure
import io.soma.cryptobook.core.network.subscription.WsSubscriptionManager
import io.soma.cryptobook.core.network.subscription.WsSubscriptionMethod
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

class CoinListStreamDataSource @Inject constructor(
    private val sessionManager: WsSessionManager,
    private val subscriptionManager: WsSubscriptionManager,
    private val tickerTable: WsTickerTable,
    private val marketMessageRouter: WsMarketMessageRouter,
) {
    private val targetStream = "!miniTicker@arr"

    // Home feature owns the lifecycle of the market overview stream for now.
    fun maintainCoinListStream(): Flow<Throwable> = flow {
        sessionManager.acquire()
        subscriptionManager.retain(setOf(targetStream))

        try {
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
                                if (message is WsMarketMessage.AllMiniTickers) {
                                    val tickers = message.tickers.map { it.toCoinTickerDto() }
                                    tickerTable.upsertAll(tickers)
                                }
                            }

                            is WsMarketStreamEvent.Transport -> {
                                when (val transportEvent = event.event) {
                                    is BinanceWebSocketClient.Event.Connected -> Unit

                                    is BinanceWebSocketClient.Event.Disconnected -> Unit

                                    is BinanceWebSocketClient.Event.Error -> {
                                        if (transportEvent.throwable is WebSocketReconnectExhaustedException) {
                                            emit(transportEvent.throwable)
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
                        val isTargetFailure = targetStream in failure.streams

                        if (isGlobalFailure || isTargetFailure) {
                            if (failure.cause is WebSocketReconnectExhaustedException) {
                                emit(failure.cause)
                            }
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
        data class Router(val event: WsMarketStreamEvent) : StreamEvent
        data class SubscriptionFailure(val failure: WsSubscriptionFailure) : StreamEvent
    }

    private fun WsMiniTickerPayload.toCoinTickerDto(): CoinTickerDto {
        val openPriceDecimal = openPrice.toBigDecimalOrNull()
        val lastPriceDecimal = lastPrice.toBigDecimalOrNull()
        val priceChange = if (openPriceDecimal != null && lastPriceDecimal != null) {
            lastPriceDecimal.subtract(openPriceDecimal)
        } else {
            BigDecimal.ZERO
        }
        val priceChangePercent = if (
            openPriceDecimal == null ||
            lastPriceDecimal == null ||
            openPriceDecimal.compareTo(BigDecimal.ZERO) == 0
        ) {
            BigDecimal.ZERO
        } else {
            priceChange
                .multiply(BigDecimal("100"))
                .divide(openPriceDecimal, 8, RoundingMode.HALF_UP)
        }

        return CoinTickerDto(
            symbol = symbol,
            lastPrice = lastPrice,
            priceChangePercent = priceChangePercent.stripTrailingZeros().toPlainString(),
            priceChange = priceChange.stripTrailingZeros().toPlainString(),
            lowPrice = lowPrice,
            highPrice = highPrice,
            quoteAssetVolume = quoteAssetVolume,
            openPrice = openPrice,
        )
    }
}

