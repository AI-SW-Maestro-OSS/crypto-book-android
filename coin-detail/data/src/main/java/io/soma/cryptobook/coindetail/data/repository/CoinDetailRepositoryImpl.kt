package io.soma.cryptobook.coindetail.data.repository

import io.soma.cryptobook.coindetail.data.datasource.CoinDetailStreamDataSource
import io.soma.cryptobook.coindetail.data.mapper.CoinDetailDomainModelMapper
import io.soma.cryptobook.coindetail.domain.model.CoinDetailStreamState
import io.soma.cryptobook.coindetail.domain.repository.CoinDetailRepository
import io.soma.cryptobook.core.data.realtime.ticker.WsTickerTable
import io.soma.cryptobook.core.domain.error.WebSocketReconnectExhaustedException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

class CoinDetailRepositoryImpl
@Inject
constructor(
    private val coinDetailStreamDataSource: CoinDetailStreamDataSource,
    private val tickerTable: WsTickerTable,
    private val coinDetailDomainModelMapper: CoinDetailDomainModelMapper,
    private val ioDispatcher: CoroutineDispatcher,
) : CoinDetailRepository {
    override fun observeCoinDetail(symbol: String): Flow<CoinDetailStreamState> = channelFlow {
        val targetSymbol = symbol.uppercase()

        val streamJob = launch {
            coinDetailStreamDataSource.observeCoinDetail(targetSymbol).collect { state ->
                when (state) {
                    is CoinDetailStreamDataSource.State.Error -> {
                        if (state.throwable is WebSocketReconnectExhaustedException) {
                            close(state.throwable)
                        }
                    }

                    is CoinDetailStreamDataSource.State.Disconnected -> {
                        Unit
                    }

                    else -> Unit
                }
            }
        }

        val tableJob = launch {
            tickerTable.observeSymbol(targetSymbol).collect { ticker ->
                if (ticker == null) {
                    send(CoinDetailStreamState.Loading)
                } else {
                    val detail = coinDetailDomainModelMapper.toDomainModel(ticker)
                    send(CoinDetailStreamState.Data(detail))
                }
            }
        }

        awaitClose {
            streamJob.cancel()
            tableJob.cancel()
        }
    }.flowOn(ioDispatcher)
}
