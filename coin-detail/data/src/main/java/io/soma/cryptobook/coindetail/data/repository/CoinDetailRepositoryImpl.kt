package io.soma.cryptobook.coindetail.data.repository

import io.soma.cryptobook.coindetail.data.datasource.CoinDetailDepthSnapshotDataSource
import io.soma.cryptobook.coindetail.data.datasource.CoinDetailKlineBackfillDataSource
import io.soma.cryptobook.coindetail.data.datasource.CoinDetailTickerSnapshotDataSource
import io.soma.cryptobook.coindetail.data.mapper.CoinDetailDomainModelMapper
import io.soma.cryptobook.coindetail.data.mapper.toDepthDiff
import io.soma.cryptobook.coindetail.data.orderbook.DepthSyncResult
import io.soma.cryptobook.coindetail.data.orderbook.OrderBookAssembler
import io.soma.cryptobook.coindetail.domain.model.CoinCandleVO
import io.soma.cryptobook.coindetail.domain.model.CoinDetailStreamState
import io.soma.cryptobook.coindetail.domain.model.OrderBookVO
import io.soma.cryptobook.coindetail.domain.repository.CoinDetailRepository
import io.soma.cryptobook.core.data.database.ticksize.SymbolTickSizeDao
import io.soma.cryptobook.core.data.model.CoinKlineDto
import io.soma.cryptobook.core.data.realtime.kline.WsKlineTable
import io.soma.cryptobook.core.data.realtime.market.MarketRealtimePayloadMapper
import io.soma.cryptobook.core.data.realtime.ticker.WsTickerTable
import io.soma.cryptobook.core.network.market.WsDepthEventPayload
import io.soma.cryptobook.core.network.market.WsMarketMessage
import io.soma.cryptobook.core.network.session.WsSessionManager
import io.soma.cryptobook.core.network.session.WsSessionState
import io.soma.cryptobook.core.network.stream.WsStreamSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class CoinDetailRepositoryImpl
@Inject
constructor(
    private val wsStreamSource: WsStreamSource,
    private val sessionManager: WsSessionManager,
    private val tickerSnapshotDataSource: CoinDetailTickerSnapshotDataSource,
    private val klineBackfillDataSource: CoinDetailKlineBackfillDataSource,
    private val depthSnapshotDataSource: CoinDetailDepthSnapshotDataSource,
    private val tickerTable: WsTickerTable,
    private val klineTable: WsKlineTable,
    private val tickSizeDao: SymbolTickSizeDao,
    private val payloadMapper: MarketRealtimePayloadMapper,
    private val coinDetailDomainModelMapper: CoinDetailDomainModelMapper,
    private val ioDispatcher: CoroutineDispatcher,
) : CoinDetailRepository {
    private companion object {
        private const val TARGET_INTERVAL = "1d"
        private const val KLINE_BACKFILL_PAGE_LIMIT = 1000
        private const val ORDER_BOOK_SNAPSHOT_LIMIT = 1000
        private const val ORDER_BOOK_DISPLAY_DEPTH = 32
    }

    override fun observeCoinDetail(symbol: String): Flow<CoinDetailStreamState> = channelFlow {
        val targetSymbol = symbol.uppercase()
        val streams = streamsFor(targetSymbol)

        val orderBookAssembler = OrderBookAssembler()
        val orderBookMutex = Mutex()
        val orderBookState = MutableStateFlow<OrderBookVO?>(null)

        suspend fun resyncOrderBook() {
            runCatching {
                depthSnapshotDataSource.getDepth(targetSymbol, ORDER_BOOK_SNAPSHOT_LIMIT)
            }.onSuccess { snapshot ->
                orderBookState.value = orderBookMutex.withLock {
                    orderBookAssembler.onSnapshot(snapshot)
                    orderBookAssembler.topOfBook(ORDER_BOOK_DISPLAY_DEPTH)
                }
            }
        }

        suspend fun handleDepthDiff(event: WsDepthEventPayload) {
            if (!event.symbol.equals(targetSymbol, ignoreCase = true)) return
            var needsResync = false
            val orderBook = orderBookMutex.withLock {
                when (orderBookAssembler.onDiff(event.toDepthDiff())) {
                    DepthSyncResult.Applied ->
                        orderBookAssembler.topOfBook(ORDER_BOOK_DISPLAY_DEPTH)

                    DepthSyncResult.Buffered -> null

                    DepthSyncResult.NeedsResync -> {
                        needsResync = true
                        null
                    }
                }
            }
            if (orderBook != null) orderBookState.value = orderBook
            if (needsResync) resyncOrderBook()
        }

        launch {
            wsStreamSource.subscribe(streams).collect { message ->
                persistToRoom(targetSymbol, message)
                if (message is WsMarketMessage.SymbolDepth) {
                    handleDepthDiff(message.depthEvent)
                }
            }
        }

        launch {
            combine(
                tickerTable.observeSymbol(targetSymbol),
                klineTable.observe(targetSymbol, TARGET_INTERVAL).map { candles ->
                    candles.map { it.toCoinCandleVO() }
                },
                tickSizeDao.observeTickSize(targetSymbol).map { it?.toBigDecimalOrNull() },
                orderBookState,
            ) { ticker, candles, tickSize, orderBook ->
                if (ticker == null) {
                    CoinDetailStreamState.Loading
                } else {
                    CoinDetailStreamState.Data(
                        value = coinDetailDomainModelMapper.toDomainModel(
                            coinTickerDto = ticker,
                            tickSize = tickSize,
                        ),
                        candles = candles,
                        orderBook = orderBook,
                    )
                }
            }.distinctUntilChanged().collect { state ->
                send(state)
            }
        }

        launch {
            sessionManager.state
                .map { it is WsSessionState.Connected }
                .distinctUntilChanged()
                .drop(1)
                .filter { it }
                .collect {
                    refreshRestData(targetSymbol)
                    resyncOrderBook()
                }
        }

        refreshRestData(targetSymbol)
        resyncOrderBook()
        awaitCancellation()
    }.flowOn(ioDispatcher)

    private suspend fun persistToRoom(targetSymbol: String, message: WsMarketMessage) {
        when (message) {
            is WsMarketMessage.SymbolTicker -> {
                val ticker = payloadMapper.toTickerDto(message.ticker)
                if (ticker.symbol == targetSymbol) {
                    tickerTable.upsert(ticker)
                }
            }

            is WsMarketMessage.SymbolKline -> {
                val candle = payloadMapper.toKlineDto(message.klineEvent)
                if (candle.symbol == targetSymbol) {
                    klineTable.upsert(
                        symbol = candle.symbol,
                        interval = candle.interval,
                        candle = candle,
                    )
                }
            }

            is WsMarketMessage.SymbolDepth,
            is WsMarketMessage.AllMiniTickers,
            WsMarketMessage.Ignored,
            -> Unit
        }
    }

    private suspend fun refreshRestData(symbol: String) = supervisorScope {
        launch {
            runCatching {
                tickerSnapshotDataSource.getTicker(symbol)
            }.onSuccess { ticker ->
                tickerTable.upsert(ticker)
            }
        }

        launch {
            runCatching {
                klineBackfillDataSource.getAllKlines(
                    symbol = symbol,
                    interval = TARGET_INTERVAL,
                    pageLimit = KLINE_BACKFILL_PAGE_LIMIT,
                )
            }.onSuccess { candles ->
                if (candles.isNotEmpty()) {
                    klineTable.replace(
                        symbol = symbol,
                        interval = TARGET_INTERVAL,
                        candles = candles,
                    )
                }
            }
        }
    }

    private fun streamsFor(symbol: String): Set<String> {
        val s = symbol.lowercase()
        return linkedSetOf("$s@ticker", "$s@kline_$TARGET_INTERVAL", "$s@depth")
    }

    private fun CoinKlineDto.toCoinCandleVO(): CoinCandleVO = CoinCandleVO(
        openTime = openTime,
        closeTime = closeTime,
        open = openPrice.toDoubleOrNull() ?: 0.0,
        close = closePrice.toDoubleOrNull() ?: 0.0,
        high = highPrice.toDoubleOrNull() ?: 0.0,
        low = lowPrice.toDoubleOrNull() ?: 0.0,
        volume = volume.toDoubleOrNull() ?: 0.0,
    )
}
