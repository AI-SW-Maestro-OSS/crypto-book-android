package io.soma.cryptobook.coindetail.data.repository

import io.soma.cryptobook.coindetail.data.datasource.CoinDetailStreamDataSource
import io.soma.cryptobook.coindetail.data.mapper.CoinDetailDomainModelMapper
import io.soma.cryptobook.coindetail.domain.model.CoinCandleVO
import io.soma.cryptobook.coindetail.domain.model.CoinDetailVO
import io.soma.cryptobook.coindetail.domain.model.CoinDetailStreamState
import io.soma.cryptobook.coindetail.domain.repository.CoinDetailRepository
import io.soma.cryptobook.core.data.model.CoinKlineDto
import io.soma.cryptobook.core.data.realtime.kline.WsKlineTable
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
    private val klineTable: WsKlineTable,
    private val coinDetailDomainModelMapper: CoinDetailDomainModelMapper,
    private val ioDispatcher: CoroutineDispatcher,
) : CoinDetailRepository {
    override fun observeCoinDetail(symbol: String): Flow<CoinDetailStreamState> = channelFlow {
        val targetSymbol = symbol.uppercase()
        val targetInterval = "1m"
        var latestDetail: CoinDetailVO? = null
        var latestCandles: List<CoinCandleVO> = emptyList()

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
                    latestDetail = null
                    send(CoinDetailStreamState.Loading)
                } else {
                    latestDetail = coinDetailDomainModelMapper.toDomainModel(ticker)
                    send(
                        CoinDetailStreamState.Data(
                            value = latestDetail!!,
                            candles = latestCandles,
                        ),
                    )
                }
            }
        }

        val klineJob = launch {
            klineTable.observe(targetSymbol, targetInterval).collect { candles ->
                latestCandles = candles.map { it.toCoinCandleVO() }
                latestDetail?.let { detail ->
                    send(
                        CoinDetailStreamState.Data(
                            value = detail,
                            candles = latestCandles,
                        ),
                    )
                }
            }
        }

        awaitClose {
            streamJob.cancel()
            tableJob.cancel()
            klineJob.cancel()
        }
    }.flowOn(ioDispatcher)

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
