package io.soma.cryptobook.home.data.repository

import io.soma.cryptobook.core.data.realtime.ticker.WsTickerTable
import io.soma.cryptobook.core.domain.error.WebSocketReconnectExhaustedException
import io.soma.cryptobook.core.domain.model.CoinInfoVO
import io.soma.cryptobook.core.domain.model.CoinPriceVO
import io.soma.cryptobook.core.domain.repository.CoinRepository
import io.soma.cryptobook.home.data.datasource.CoinListRemoteDataSource
import io.soma.cryptobook.home.data.datasource.CoinListStreamDataSource
import io.soma.cryptobook.home.data.mapper.CoinPriceDomainModelMapper
import io.soma.cryptobook.home.data.model.toCoinPriceVO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CoinRepositoryImpl
@Inject
constructor(
    private val coinListRemoteDataSource: CoinListRemoteDataSource,
    private val coinListStreamDataSource: CoinListStreamDataSource,
    private val tickerTable: WsTickerTable,
    private val coinPriceDomainModelMapper: CoinPriceDomainModelMapper,
    private val ioDispatcher: CoroutineDispatcher,
) : CoinRepository {
    override suspend fun getCoinPrices(): List<CoinPriceVO> = withContext(ioDispatcher) {
        coinListRemoteDataSource.getAllTickerPrices().map { it.toCoinPriceVO() }
    }

    override fun observeCoinPrices(): Flow<List<CoinPriceVO>> = flow {
        val initialPrices = LinkedHashMap<String, CoinPriceVO>()
        val initial = runCatching { getCoinPrices() }
            .getOrDefault(emptyList())
            .also { prices ->
                prices.forEach { initialPrices[it.symbol] = it }
            }

        emit(initial)

        val tableUpdates = tickerTable.table
            .filter { it.isNotEmpty() }
            .map { table ->
                val merged = LinkedHashMap(initialPrices)
                table.values.forEach { ticker ->
                    merged[ticker.symbol] = coinPriceDomainModelMapper.toDomainModel(ticker)
                }
                merged.values.toList()
            }

        val fatalErrors = flow<List<CoinPriceVO>> {
            coinListStreamDataSource.maintainCoinListStream().collect { throwable ->
                if (throwable is WebSocketReconnectExhaustedException) {
                    throw throwable
                }
            }
        }

        emitAll(merge(tableUpdates, fatalErrors))
    }.flowOn(ioDispatcher)

    override suspend fun getCoinInfoList(): List<CoinInfoVO> {
        TODO("Not yet implemented")
    }
}
