package io.soma.cryptobook.home.data.repository

import io.soma.cryptobook.core.data.database.ticksize.SymbolTickSizeDao
import io.soma.cryptobook.core.data.realtime.ticker.WsTickerTable
import io.soma.cryptobook.core.domain.model.CoinInfoVO
import io.soma.cryptobook.core.domain.model.CoinPriceVO
import io.soma.cryptobook.core.domain.repository.CoinRepository
import io.soma.cryptobook.home.data.datasource.CoinListRemoteDataSource
import io.soma.cryptobook.home.data.mapper.CoinPriceDomainModelMapper
import io.soma.cryptobook.home.data.model.toCoinPriceVO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject

class CoinRepositoryImpl
@Inject
constructor(
    private val coinListRemoteDataSource: CoinListRemoteDataSource,
    private val tickerTable: WsTickerTable,
    private val tickSizeDao: SymbolTickSizeDao,
    private val coinPriceDomainModelMapper: CoinPriceDomainModelMapper,
    private val ioDispatcher: CoroutineDispatcher,
) : CoinRepository {
    override suspend fun getCoinPrices(): List<CoinPriceVO> = withContext(ioDispatcher) {
        coinListRemoteDataSource.getAllTickerPrices().map { it.toCoinPriceVO() }
    }

    override fun observeCoinPrices(): Flow<List<CoinPriceVO>> {
        val pricesFlow = flow {
            val initialPrices = LinkedHashMap<String, CoinPriceVO>()
            val initial = runCatching { getCoinPrices() }
                .getOrDefault(emptyList())
                .also { prices ->
                    prices.forEach { initialPrices[it.symbol] = it }
                }

            val currentTable = tickerTable.table.value
            val initialEmission = if (currentTable.isEmpty()) {
                initial
            } else {
                mergePrices(
                    initialPrices = initialPrices,
                    table = currentTable,
                )
            }

            emit(initialEmission)

            val tableUpdates = tickerTable.table
                .let { tableFlow ->
                    if (currentTable.isEmpty()) tableFlow else tableFlow.drop(1)
                }
                .filter { it.isNotEmpty() }
                .map { table -> mergePrices(initialPrices = initialPrices, table = table) }

            emitAll(tableUpdates)
        }

        val tickSizesFlow = tickSizeDao.observeAll().map { entities ->
            entities.associate { it.symbol to it.tickSize.toBigDecimalOrNull() }
        }

        return combine(pricesFlow, tickSizesFlow) { prices, tickSizes ->
            prices.withTickSizes(tickSizes)
        }.flowOn(ioDispatcher)
    }

    override suspend fun getCoinInfoList(): List<CoinInfoVO> {
        TODO("Not yet implemented")
    }

    private fun mergePrices(
        initialPrices: Map<String, CoinPriceVO>,
        table: Map<String, io.soma.cryptobook.core.data.model.CoinTickerDto>,
    ): List<CoinPriceVO> {
        val merged = LinkedHashMap(initialPrices)
        table.values.forEach { ticker ->
            merged[ticker.symbol] = coinPriceDomainModelMapper.toDomainModel(ticker)
        }
        return merged.values.toList()
    }

    private fun List<CoinPriceVO>.withTickSizes(
        tickSizes: Map<String, BigDecimal?>,
    ): List<CoinPriceVO> = map { coinPrice ->
        coinPrice.copy(tickSize = tickSizes[coinPrice.symbol])
    }
}
