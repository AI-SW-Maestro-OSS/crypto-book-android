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
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
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

    override fun observeCoinPrices(): Flow<List<CoinPriceVO>> =
        coinListStreamDataSource.observeTickers()
            .onEach { state ->
                if (state is CoinListStreamDataSource.State.Error &&
                    state.throwable is WebSocketReconnectExhaustedException
                ) {
                    throw state.throwable
                }
            }
            .filterIsInstance<CoinListStreamDataSource.State.Success>()
            .map { state ->
                state.tickers.forEach { ticker ->
                    cache[ticker.symbol] = coinPriceDomainModelMapper.toDomainModel(ticker)
                }
                cache.values.toList()
            }
            .onStart {
                if (cache.isEmpty()) {
                    try {
                        val initialData = getCoinPrices()
                        initialData.forEach { cache[it.symbol] = it }
                    } catch (e: Exception) {
                        // initial load failed, will retry on stream
                    }
                }
                if (cache.isNotEmpty()) {
                    emit(cache.values.toList())
                }
            }
            .flowOn(ioDispatcher)

    override suspend fun getCoinInfoList(): List<CoinInfoVO> {
        TODO("Not yet implemented")
    }
}
