package io.soma.cryptobook.core.data.realtime.market

import io.soma.cryptobook.core.data.realtime.kline.WsKlineTable
import io.soma.cryptobook.core.data.realtime.ticker.WsTickerTable
import io.soma.cryptobook.core.domain.error.WebSocketReconnectExhaustedException
import io.soma.cryptobook.core.network.BinanceWebSocketClient
import io.soma.cryptobook.core.network.market.WsMarketMessage
import io.soma.cryptobook.core.network.market.WsMarketMessageRouter
import io.soma.cryptobook.core.network.session.WsSessionManager
import io.soma.cryptobook.core.network.session.WsSessionState
import io.soma.cryptobook.core.network.subscription.WsSubscriptionFailure
import io.soma.cryptobook.core.network.subscription.WsSubscriptionManager
import io.soma.cryptobook.core.network.subscription.WsSubscriptionMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class DefaultMarketRealtimeCoordinator @Inject constructor(
    private val sessionManager: WsSessionManager,
    private val subscriptionManager: WsSubscriptionManager,
    private val marketMessageRouter: WsMarketMessageRouter,
    private val tickerTable: WsTickerTable,
    private val klineTable: WsKlineTable,
    private val payloadMapper: MarketRealtimePayloadMapper,
    scope: CoroutineScope,
) : MarketRealtimeCoordinator {

    private val mutex = Mutex()
    private val overviewStream = "!miniTicker@arr"
    private val symbolDemandCounts = LinkedHashMap<String, Int>()
    private var overviewDemandCount = 0
    private var started = false
    private var sessionLeaseHeld = false

    private val _runtimeState = MutableStateFlow(
        MarketRealtimeRuntimeState.initial(nowMillis = currentTimeMillis()),
    )
    override val runtimeState: StateFlow<MarketRealtimeRuntimeState> = _runtimeState.asStateFlow()

    private val _demandFatalSignals = MutableSharedFlow<MarketRealtimeDemandFatalSignal>(
        extraBufferCapacity = 32,
    )
    override val demandFatalSignals: SharedFlow<MarketRealtimeDemandFatalSignal> =
        _demandFatalSignals.asSharedFlow()

    init {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            sessionManager.state.collect { state ->
                applySessionState(state)
            }
        }
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            sessionManager.events.collect { event ->
                if (
                    event is BinanceWebSocketClient.Event.Error &&
                    event.throwable is WebSocketReconnectExhaustedException
                ) {
                    updateRuntimeState(
                        isConnected = false,
                        isRecovering = false,
                        lastFatalError = event.throwable,
                    )
                }
            }
        }
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            marketMessageRouter.events.collect { message ->
                handleMarketMessage(message)
            }
        }
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            subscriptionManager.failures.collect { failure ->
                handleSubscriptionFailure(failure)
            }
        }
    }

    @Synchronized
    override fun start() {
        if (started) return
        started = true
        if (!sessionLeaseHeld) {
            sessionManager.acquire()
            sessionLeaseHeld = true
        }
        updateRuntimeState(isStarted = true)
    }

    @Synchronized
    override fun stop() {
        if (!started && !sessionLeaseHeld) return
        started = false
        if (sessionLeaseHeld) {
            sessionManager.release()
            sessionLeaseHeld = false
        }
        updateRuntimeState(
            isStarted = false,
            isConnected = false,
            isRecovering = false,
        )
    }

    override suspend fun retainOverview() {
        val shouldRetain = mutex.withLock {
            overviewDemandCount += 1
            overviewDemandCount == 1
        }
        if (shouldRetain) {
            subscriptionManager.retain(setOf(overviewStream))
        }
    }

    override suspend fun releaseOverview() {
        val shouldRelease = mutex.withLock {
            if (overviewDemandCount == 0) {
                false
            } else {
                overviewDemandCount -= 1
                overviewDemandCount == 0
            }
        }
        if (shouldRelease) {
            subscriptionManager.release(setOf(overviewStream))
        }
    }

    override suspend fun retainSymbol(symbol: String) {
        val normalizedSymbol = normalizeSymbol(symbol)
        val shouldRetain = mutex.withLock {
            val previous = symbolDemandCounts[normalizedSymbol] ?: 0
            symbolDemandCounts[normalizedSymbol] = previous + 1
            previous == 0
        }
        if (shouldRetain) {
            subscriptionManager.retain(streamsForSymbol(normalizedSymbol))
        }
    }

    override suspend fun releaseSymbol(symbol: String) {
        val normalizedSymbol = normalizeSymbol(symbol)
        val shouldRelease = mutex.withLock {
            when (val previous = symbolDemandCounts[normalizedSymbol] ?: 0) {
                0 -> false
                1 -> {
                    symbolDemandCounts.remove(normalizedSymbol)
                    true
                }
                else -> {
                    symbolDemandCounts[normalizedSymbol] = previous - 1
                    false
                }
            }
        }
        if (shouldRelease) {
            subscriptionManager.release(streamsForSymbol(normalizedSymbol))
        }
    }

    private suspend fun handleMarketMessage(message: WsMarketMessage) {
        when (message) {
            is WsMarketMessage.AllMiniTickers -> {
                if (!hasOverviewDemand()) return
                tickerTable.upsertAll(message.tickers.map(payloadMapper::toTickerDto))
            }

            is WsMarketMessage.SymbolTicker -> {
                val ticker = payloadMapper.toTickerDto(message.ticker)
                if (!hasSymbolDemand(ticker.symbol)) return
                tickerTable.upsert(ticker)
            }

            is WsMarketMessage.SymbolKline -> {
                val candle = payloadMapper.toKlineDto(message.klineEvent)
                if (!hasSymbolDemand(candle.symbol)) return
                klineTable.upsert(
                    symbol = candle.symbol,
                    interval = candle.interval,
                    candle = candle,
                )
            }

            WsMarketMessage.Ignored -> Unit
        }
    }

    private suspend fun handleSubscriptionFailure(failure: WsSubscriptionFailure) {
        val activeDemands = mutex.withLock {
            ActiveDemands(
                hasOverview = overviewDemandCount > 0,
                symbols = symbolDemandCounts.keys.toSet(),
            )
        }
        val signals = LinkedHashSet<MarketRealtimeDemandFatalSignal>()

        if (failure.method == WsSubscriptionMethod.ListSubscriptions) {
            if (activeDemands.hasOverview) {
                signals += MarketRealtimeDemandFatalSignal.Overview(failure.cause)
            }
            activeDemands.symbols.forEach { symbol ->
                signals += MarketRealtimeDemandFatalSignal.Symbol(symbol, failure.cause)
            }
        } else {
            failure.streams.forEach { stream ->
                when {
                    stream == overviewStream && activeDemands.hasOverview -> {
                        signals += MarketRealtimeDemandFatalSignal.Overview(failure.cause)
                    }

                    else -> streamToSymbol(stream)
                        ?.takeIf { it in activeDemands.symbols }
                        ?.let { symbol ->
                            signals += MarketRealtimeDemandFatalSignal.Symbol(symbol, failure.cause)
                        }
                }
            }
        }

        signals.forEach { signal ->
            _demandFatalSignals.tryEmit(signal)
        }
    }

    private fun applySessionState(state: WsSessionState) {
        when (state) {
            WsSessionState.Idle,
            WsSessionState.Stopped,
            -> updateRuntimeState(isConnected = false, isRecovering = false)

            is WsSessionState.Connecting,
            is WsSessionState.Reconnecting,
            is WsSessionState.Rotating,
            -> updateRuntimeState(isConnected = false, isRecovering = true)

            is WsSessionState.Connected -> updateRuntimeState(isConnected = true, isRecovering = false)

            is WsSessionState.Exhausted -> updateRuntimeState(
                isConnected = false,
                isRecovering = false,
                lastFatalError = state.cause,
            )
        }
    }

    private suspend fun hasOverviewDemand(): Boolean = mutex.withLock { overviewDemandCount > 0 }

    private suspend fun hasSymbolDemand(symbol: String): Boolean = mutex.withLock {
        (symbolDemandCounts[normalizeSymbol(symbol)] ?: 0) > 0
    }

    private fun normalizeSymbol(symbol: String): String = symbol.uppercase()

    private fun streamsForSymbol(symbol: String): Set<String> {
        val normalized = normalizeSymbol(symbol).lowercase()
        return linkedSetOf(
            "${normalized}@ticker",
            "${normalized}@kline_1d",
        )
    }

    private fun streamToSymbol(stream: String): String? {
        if (!stream.contains('@')) return null
        val symbol = stream.substringBefore('@')
        return if (symbol.isBlank() || symbol == "!miniTicker") null else symbol.uppercase()
    }

    private fun updateRuntimeState(
        isStarted: Boolean = _runtimeState.value.isStarted,
        isConnected: Boolean = _runtimeState.value.isConnected,
        isRecovering: Boolean = _runtimeState.value.isRecovering,
        lastFatalError: Throwable? = _runtimeState.value.lastFatalError,
    ) {
        _runtimeState.value = MarketRealtimeRuntimeState(
            isStarted = isStarted,
            isConnected = isConnected,
            isRecovering = isRecovering,
            lastFatalError = lastFatalError,
            lastUpdatedAtMillis = currentTimeMillis(),
        )
    }

    private fun currentTimeMillis(): Long = System.currentTimeMillis()

    private data class ActiveDemands(
        val hasOverview: Boolean,
        val symbols: Set<String>,
    )
}
