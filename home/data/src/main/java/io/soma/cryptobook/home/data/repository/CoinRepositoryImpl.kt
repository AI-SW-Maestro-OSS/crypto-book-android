package io.soma.cryptobook.home.data.repository

import io.soma.cryptobook.core.data.database.ticksize.SymbolTickSizeDao
import io.soma.cryptobook.core.data.realtime.ticker.WsTickerTable
import io.soma.cryptobook.core.domain.error.CoinPriceError
import io.soma.cryptobook.core.domain.error.HttpResponseStatus
import io.soma.cryptobook.core.domain.model.CoinInfoVO
import io.soma.cryptobook.core.domain.model.CoinPriceVO
import io.soma.cryptobook.core.domain.outcome.Outcome
import io.soma.cryptobook.core.domain.outcome.mapSuccess
import io.soma.cryptobook.core.domain.repository.CoinRepository
import io.soma.cryptobook.core.network.error.ApiError
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
    override suspend fun getCoinPrices(): Outcome<List<CoinPriceVO>, CoinPriceError> = withContext(
        ioDispatcher,
    ) {
        when (val outcome = coinListRemoteDataSource.getAllTickerPrices()) {
            is Outcome.Success -> {
                try {
                    Outcome.success(outcome.data.map { it.toCoinPriceVO() })
                } catch (e: NumberFormatException) {
                    Outcome.Failure(
                        error = CoinPriceError.UnexpectedResponse,
                        cause = e,
                    )
                }
            }

            is Outcome.Failure -> {
                Outcome.Failure(
                    error = outcome.error.toCoinPriceError(),
                    cause = outcome.cause,
                )
            }
        }
    }

    override fun observeCoinPrices(): Flow<Outcome<List<CoinPriceVO>, CoinPriceError>> {
        val pricesFlow = flow {
            val initialPrices = LinkedHashMap<String, CoinPriceVO>()
            when (val initialOutcome = getCoinPrices()) {
                is Outcome.Success -> {
                    val initial = initialOutcome.data
                    initial.forEach { initialPrices[it.symbol] = it }

                    val currentTable = tickerTable.table.value
                    val initialEmission = if (currentTable.isEmpty()) {
                        initial
                    } else {
                        mergePrices(
                            initialPrices = initialPrices,
                            table = currentTable,
                        )
                    }

                    emit(Outcome.success(initialEmission))
                }

                is Outcome.Failure -> {
                    emit(initialOutcome)
                }
            }

            val currentTable = tickerTable.table.value

            val tableUpdates = tickerTable.table
                .let { tableFlow ->
                    if (currentTable.isEmpty()) tableFlow else tableFlow.drop(1)
                }
                .filter { it.isNotEmpty() }
                .map { table ->
                    Outcome.success(
                        mergePrices(
                            initialPrices = initialPrices,
                            table = table,
                        ),
                    )
                }

            emitAll(tableUpdates)
        }

        val tickSizesFlow = tickSizeDao.observeAll().map { entities ->
            entities.associate { it.symbol to it.tickSize.toBigDecimalOrNull() }
        }

        return combine(pricesFlow, tickSizesFlow) { pricesOutcome, tickSizes ->
            pricesOutcome.mapSuccess { prices ->
                prices.withTickSizes(tickSizes)
            }
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

    private fun ApiError.toCoinPriceError(): CoinPriceError {
        return when (this) {
            ApiError.Network -> CoinPriceError.Network

            is ApiError.Http -> when {
                status == HttpResponseStatus.RateLimitExceeded ||
                    status == HttpResponseStatus.IpBanned -> CoinPriceError.RateLimited

                status == HttpResponseStatus.InternalError ||
                    status == HttpResponseStatus.Unavailable ||
                    rawCode in 500..599 -> CoinPriceError.Server

                status == HttpResponseStatus.ClientTimeout -> CoinPriceError.Network

                else -> CoinPriceError.Unknown(message)
            }

            is ApiError.UnexpectedBody -> CoinPriceError.UnexpectedResponse

            is ApiError.Unknown -> CoinPriceError.Unknown(message)
        }
    }
}
