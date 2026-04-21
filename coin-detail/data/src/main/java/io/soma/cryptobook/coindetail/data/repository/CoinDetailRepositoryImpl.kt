package io.soma.cryptobook.coindetail.data.repository

import io.soma.cryptobook.coindetail.data.datasource.CoinDetailKlineBackfillDataSource
import io.soma.cryptobook.coindetail.data.datasource.CoinDetailTickerSnapshotDataSource
import io.soma.cryptobook.coindetail.data.mapper.CoinDetailDomainModelMapper
import io.soma.cryptobook.coindetail.domain.model.CoinCandleVO
import io.soma.cryptobook.coindetail.domain.model.CoinDetailStreamState
import io.soma.cryptobook.coindetail.domain.repository.CoinDetailRepository
import io.soma.cryptobook.core.data.database.ticksize.SymbolTickSizeDao
import io.soma.cryptobook.core.data.model.CoinKlineDto
import io.soma.cryptobook.core.data.realtime.kline.WsKlineTable
import io.soma.cryptobook.core.data.realtime.market.MarketRealtimeCoordinator
import io.soma.cryptobook.core.data.realtime.market.MarketRealtimeDemandFatalSignal
import io.soma.cryptobook.core.data.realtime.ticker.WsTickerTable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CoinDetailRepositoryImpl
@Inject
constructor(
    private val marketRealtimeCoordinator: MarketRealtimeCoordinator,
    private val tickerSnapshotDataSource: CoinDetailTickerSnapshotDataSource,
    private val klineBackfillDataSource: CoinDetailKlineBackfillDataSource,
    private val tickerTable: WsTickerTable,
    private val klineTable: WsKlineTable,
    private val tickSizeDao: SymbolTickSizeDao,
    private val coinDetailDomainModelMapper: CoinDetailDomainModelMapper,
    private val ioDispatcher: CoroutineDispatcher,
) : CoinDetailRepository {
    private companion object {
        private const val TARGET_INTERVAL = "1d"
        private const val KLINE_BACKFILL_PAGE_LIMIT = 1000
    }

    override fun observeCoinDetail(symbol: String): Flow<CoinDetailStreamState> = channelFlow {
        val targetSymbol = symbol.uppercase()

        marketRealtimeCoordinator.retainSymbol(targetSymbol)
        try {
            launch {
                combine(
                    tickerTable.observeSymbol(targetSymbol),
                    klineTable.observe(targetSymbol, TARGET_INTERVAL).map { candles ->
                        candles.map { it.toCoinCandleVO() }
                    },
                    tickSizeDao.observeTickSize(targetSymbol).map { it?.toBigDecimalOrNull() },
                ) { ticker, candles, tickSize ->
                    if (ticker == null) {
                        CoinDetailStreamState.Loading
                    } else {
                        CoinDetailStreamState.Data(
                            value = coinDetailDomainModelMapper.toDomainModel(
                                coinTickerDto = ticker,
                                tickSize = tickSize,
                            ),
                            candles = candles,
                        )
                    }
                }.collect { state ->
                    send(state)
                }
            }

            launch {
                marketRealtimeCoordinator.demandFatalSignals
                    .filterIsInstance<MarketRealtimeDemandFatalSignal.Symbol>()
                    .filter { it.symbol == targetSymbol }
                    .collect { signal ->
                        throw signal.cause
                    }
            }

            launch {
                marketRealtimeCoordinator.runtimeState
                    .map { it.isConnected }
                    .distinctUntilChanged()
                    .drop(1)
                    .filter { it }
                    .collect {
                        refreshRestData(targetSymbol)
                    }
            }

            refreshRestData(targetSymbol)
            awaitCancellation()
        } finally {
            withContext(NonCancellable) {
                marketRealtimeCoordinator.releaseSymbol(targetSymbol)
            }
        }
    }.flowOn(ioDispatcher)

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
