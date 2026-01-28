package io.soma.cryptobook.home.data.repository

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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CoinRepositoryImpl
@Inject
constructor(
    private val coinListRemoteDataSource: CoinListRemoteDataSource,
    private val coinListStreamDataSource: CoinListStreamDataSource,
    private val coinPriceDomainModelMapper: CoinPriceDomainModelMapper,
    private val ioDispatcher: CoroutineDispatcher,
) : CoinRepository {

    private val cache = LinkedHashMap<String, CoinPriceVO>()

    override suspend fun getCoinPrices(): List<CoinPriceVO> = withContext(ioDispatcher) {
        coinListRemoteDataSource.getAllTickerPrices().map { it.toCoinPriceVO() }
    }

    override fun observeCoinPrices(): Flow<List<CoinPriceVO>> = flow {
        if (cache.isEmpty()) {
            try {
                val initialData = getCoinPrices()
                initialData.forEach { cache[it.symbol] = it }
            } catch (e: Exception) {
            }
        }

        if (cache.isNotEmpty()) {
            emit(cache.values.toList())
        }

        coinListStreamDataSource.observeCoinList().collect { state ->
            when (state) {
                is CoinListStreamDataSource.State.Success -> {
                    state.tickers.forEach { ticker ->
                        cache[ticker.symbol] = coinPriceDomainModelMapper.toDomainModel(ticker)
                    }
                    emit(cache.values.toList())
                }

                is CoinListStreamDataSource.State.Connected -> {
                    if (cache.isNotEmpty()) emit(cache.values.toList())
                }

                is CoinListStreamDataSource.State.Error -> {
                    if (state.throwable is WebSocketReconnectExhaustedException) {
                        throw state.throwable
                    }
                }

                is CoinListStreamDataSource.State.Disconnected -> {}
            }
        }
    }.flowOn(ioDispatcher)

    override suspend fun getCoinInfoList(): List<CoinInfoVO> {
        TODO("Not yet implemented")
    }
}
