package io.soma.cryptobook.coindetail.data.repository

import io.soma.cryptobook.coindetail.data.datasource.CoinDetailStreamDataSource
import io.soma.cryptobook.coindetail.data.mapper.CoinDetailDomainModelMapper
import io.soma.cryptobook.coindetail.domain.model.CoinDetailVO
import io.soma.cryptobook.coindetail.domain.repository.CoinDetailRepository
import io.soma.cryptobook.core.domain.error.WebSocketReconnectExhaustedException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class CoinDetailRepositoryImpl
@Inject
constructor(
    private val coinDetailStreamDataSource: CoinDetailStreamDataSource,
    private val coinDetailDomainModelMapper: CoinDetailDomainModelMapper,
    private val ioDispatcher: CoroutineDispatcher,
) : CoinDetailRepository {
    private var cachedDetail: CoinDetailVO? = null

    override fun observeCoinDetail(symbol: String): Flow<CoinDetailVO> = flow {
        coinDetailStreamDataSource.observeCoinDetail(symbol).collect { state ->
            when (state) {
                is CoinDetailStreamDataSource.State.Success -> {
                    val coinDetail = coinDetailDomainModelMapper.toDomainModel(state.ticker)
                    cachedDetail = coinDetail
                    emit(coinDetail)
                }

                is CoinDetailStreamDataSource.State.Connected -> {
                    cachedDetail?.let { emit(it) }
                }

                is CoinDetailStreamDataSource.State.Error -> {
                    if (state.throwable is WebSocketReconnectExhaustedException) {
                        throw state.throwable
                    }
                }

                is CoinDetailStreamDataSource.State.Disconnected -> {
                }
            }
        }
    }.flowOn(ioDispatcher)
}
