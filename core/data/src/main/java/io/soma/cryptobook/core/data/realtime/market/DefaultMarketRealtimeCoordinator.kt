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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DefaultMarketRealtimeCoordinator @Inject constructor(
    private val sessionManager: WsSessionManager,
    private val subscriptionManager: WsSubscriptionManager,
    private val marketMessageRouter: WsMarketMessageRouter,
    private val tickerTable: WsTickerTable,
    private val klineTable: WsKlineTable,
    private val payloadMapper: MarketRealtimePayloadMapper,
    private val scope: CoroutineScope,
) : MarketRealtimeCoordinator {

    private val overviewStream = "!miniTicker@arr"
    private val commands = Channel<Command>(capacity = Channel.UNLIMITED)
    private val symbolDemandCounts = LinkedHashMap<String, Int>()
    private var started = false
    private var sessionLeaseHeld = false
    private var overviewRetained = false
    private val activeDemands = MutableStateFlow(ActiveDemands())

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
            for (command in commands) {
                when (command) {
                    Command.Start -> handleStart()
                    Command.Stop -> handleStop()
                    is Command.RetainSymbol -> handleRetainSymbol(command.symbol, command.ack)
                    is Command.ReleaseSymbol -> handleReleaseSymbol(command.symbol, command.ack)
                }
            }
        }
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

    override fun start() {
        commands.trySend(Command.Start)
    }

    override fun stop() {
        commands.trySend(Command.Stop)
    }

    override suspend fun retainSymbol(symbol: String) {
        val ack = CompletableDeferred<Unit>()
        commands.send(Command.RetainSymbol(symbol = normalizeSymbol(symbol), ack = ack))
        ack.await()
    }

    override suspend fun releaseSymbol(symbol: String) {
        val ack = CompletableDeferred<Unit>()
        commands.send(Command.ReleaseSymbol(symbol = normalizeSymbol(symbol), ack = ack))
        ack.await()
    }

    private suspend fun handleMarketMessage(message: WsMarketMessage) {
        when (message) {
            is WsMarketMessage.AllMiniTickers -> {
                if (!activeDemands.value.hasOverview) return
                tickerTable.upsertAll(message.tickers.map(payloadMapper::toTickerDto))
            }

            is WsMarketMessage.SymbolTicker -> {
                val ticker = payloadMapper.toTickerDto(message.ticker)
                if (!activeDemands.value.hasSymbolDemand(ticker.symbol)) return
                tickerTable.upsert(ticker)
            }

            is WsMarketMessage.SymbolKline -> {
                val candle = payloadMapper.toKlineDto(message.klineEvent)
                if (!activeDemands.value.hasSymbolDemand(candle.symbol)) return
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
        val activeDemands = activeDemands.value
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
            -> updateRuntimeState(
                isConnected = false,
                isRecovering = false,
                lastFatalError = null,
            )

            is WsSessionState.Reconnecting,
            is WsSessionState.Rotating,
            -> updateRuntimeState(
                isConnected = false,
                isRecovering = true,
                lastFatalError = null,
            )

            is WsSessionState.Connecting -> updateRuntimeState(
                isConnected = false,
                isRecovering = false,
                lastFatalError = null,
            )

            is WsSessionState.Connected -> updateRuntimeState(
                isConnected = true,
                isRecovering = false,
                lastFatalError = null,
            )

            is WsSessionState.Exhausted -> updateRuntimeState(
                isConnected = false,
                isRecovering = false,
                lastFatalError = state.cause,
            )
        }
    }

    private suspend fun handleStart() {
        if (started) return
        started = true
        if (!sessionLeaseHeld) {
            sessionManager.acquire()
            sessionLeaseHeld = true
        }
        if (!overviewRetained) {
            subscribeOverviewBaseline()
        }
        updateRuntimeState(isStarted = true)
    }

    private suspend fun handleStop() {
        if (!started && !sessionLeaseHeld) return
        started = false
        if (overviewRetained) {
            unsubscribeOverviewBaseline()
        }
        if (sessionLeaseHeld) {
            sessionManager.release()
            sessionLeaseHeld = false
        }
        updateRuntimeState(
            isStarted = false,
            isConnected = false,
            isRecovering = false,
            lastFatalError = null,
        )
    }

    private suspend fun handleRetainSymbol(
        symbol: String,
        ack: CompletableDeferred<Unit>,
    ) {
        runCommand(ack) {
            val previous = symbolDemandCounts[symbol] ?: 0
            symbolDemandCounts[symbol] = previous + 1
            publishActiveDemands()
            if (previous == 0) {
                subscriptionManager.retain(streamsForSymbol(symbol))
            }
        }
    }

    private suspend fun handleReleaseSymbol(
        symbol: String,
        ack: CompletableDeferred<Unit>,
    ) {
        runCommand(ack) {
            when (val previous = symbolDemandCounts[symbol] ?: 0) {
                0 -> Unit
                1 -> {
                    symbolDemandCounts.remove(symbol)
                    publishActiveDemands()
                    subscriptionManager.release(streamsForSymbol(symbol))
                }
                else -> {
                    symbolDemandCounts[symbol] = previous - 1
                    publishActiveDemands()
                }
            }
        }
    }

    private suspend fun subscribeOverviewBaseline() {
        overviewRetained = true
        publishActiveDemands()
        subscriptionManager.retain(setOf(overviewStream))
    }

    private suspend fun unsubscribeOverviewBaseline() {
        overviewRetained = false
        publishActiveDemands()
        subscriptionManager.release(setOf(overviewStream))
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
        val hasOverview: Boolean = false,
        val symbols: Set<String>,
    ) {
        constructor() : this(symbols = emptySet())

        fun hasSymbolDemand(symbol: String): Boolean = normalize(symbol) in symbols

        private fun normalize(symbol: String): String = symbol.uppercase()
    }

    private fun publishActiveDemands() {
        activeDemands.value = ActiveDemands(
            hasOverview = overviewRetained,
            symbols = symbolDemandCounts.keys.toSet(),
        )
    }

    private suspend fun runCommand(
        ack: CompletableDeferred<Unit>,
        block: suspend () -> Unit,
    ) {
        try {
            block()
            ack.complete(Unit)
        } catch (throwable: Throwable) {
            ack.completeExceptionally(throwable)
        }
    }

    private sealed interface Command {
        data object Start : Command
        data object Stop : Command
        data class RetainSymbol(
            val symbol: String,
            val ack: CompletableDeferred<Unit>,
        ) : Command
        data class ReleaseSymbol(
            val symbol: String,
            val ack: CompletableDeferred<Unit>,
        ) : Command
    }
}
