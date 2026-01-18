package io.soma.cryptobook.coindetail.data.repository

import io.soma.cryptobook.coindetail.data.datasource.CoinDetailStreamDataSource
import io.soma.cryptobook.coindetail.domain.repository.CoinDetailRepository
import io.soma.cryptobook.core.data.model.toCoinPriceVO
import io.soma.cryptobook.core.domain.error.WebSocketDisconnectedException
import io.soma.cryptobook.core.domain.model.CoinPriceVO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class CoinDetailRepositoryImpl
@Inject
constructor(
    private val coinDetailStreamDataSource: CoinDetailStreamDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) : CoinDetailRepository {
    private var cachedDetail: CoinPriceVO? = null

    override fun observeCoinDetail(symbol: String): Flow<CoinPriceVO> = flow {
        coinDetailStreamDataSource.observeCoinDetail(symbol).collect { state ->
            when (state) {
                is CoinDetailStreamDataSource.State.Success -> {
                    val coinPrice = state.ticker.toCoinPriceVO()
                    cachedDetail = coinPrice
                    emit(coinPrice)
                }

                is CoinDetailStreamDataSource.State.Connected -> {
                    cachedDetail?.let { emit(it) }
                }

                is CoinDetailStreamDataSource.State.Error,
                is CoinDetailStreamDataSource.State.Disconnected -> {
                }
            }
        }
    }.flowOn(ioDispatcher)
}
