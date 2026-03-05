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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
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

    override fun observeCoinPrices(): Flow<List<CoinPriceVO>> = channelFlow {
        val initialPrices = LinkedHashMap<String, CoinPriceVO>()
        var hasInitialEmission = false

        runCatching { getCoinPrices() }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?.let { initial ->
                initial.forEach { initialPrices[it.symbol] = it }
                send(initial)
                hasInitialEmission = true
            }

        if (!hasInitialEmission) {
            send(emptyList())
        }

        val streamJob = launch {
            coinListStreamDataSource.observeCoinList().collect { state ->
                when (state) {
                    is CoinListStreamDataSource.State.Error -> {
                        if (state.throwable is WebSocketReconnectExhaustedException) {
                            close(state.throwable)
                        }
                    }

                    is CoinListStreamDataSource.State.Disconnected -> {
                        send(emptyList())
                    }

                    else -> Unit
                }
            }
        }

        val tableJob = launch {
            tickerTable.table.collect { table ->
                if (table.isEmpty()) return@collect
                val merged = LinkedHashMap(initialPrices)
                table.values.forEach { ticker ->
                    merged[ticker.symbol] = coinPriceDomainModelMapper.toDomainModel(ticker)
                }
                send(merged.values.toList())
            }
        }

        awaitClose {
            streamJob.cancel()
            tableJob.cancel()
        }
    }.flowOn(ioDispatcher)

    override suspend fun getCoinInfoList(): List<CoinInfoVO> {
        TODO("Not yet implemented")
    }
}
