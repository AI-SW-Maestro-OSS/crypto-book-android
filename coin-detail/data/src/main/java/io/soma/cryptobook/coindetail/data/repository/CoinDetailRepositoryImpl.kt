package io.soma.cryptobook.coindetail.data.repository

import io.soma.cryptobook.coindetail.data.datasource.CoinDetailStreamDataSource
import io.soma.cryptobook.coindetail.data.mapper.CoinDetailDomainModelMapper
import io.soma.cryptobook.coindetail.domain.model.CoinCandleVO
import io.soma.cryptobook.coindetail.domain.model.CoinDetailStreamState
import io.soma.cryptobook.coindetail.domain.repository.CoinDetailRepository
import io.soma.cryptobook.core.data.model.CoinKlineDto
import io.soma.cryptobook.core.data.realtime.kline.WsKlineTable
import io.soma.cryptobook.core.data.realtime.ticker.WsTickerTable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
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
    override fun observeCoinDetail(symbol: String): Flow<CoinDetailStreamState> = flow {
        val targetSymbol = symbol.uppercase()
        val targetInterval = "1d"

        val detailStates = combine(
            tickerTable.observeSymbol(targetSymbol),
            klineTable.observe(targetSymbol, targetInterval).map { candles ->
                candles.map { it.toCoinCandleVO() }
            },
        ) { ticker, candles ->
            if (ticker == null) {
                CoinDetailStreamState.Loading
            } else {
                CoinDetailStreamState.Data(
                    value = coinDetailDomainModelMapper.toDomainModel(ticker),
                    candles = candles,
                )
            }
        }

        val fatalErrors = flow<CoinDetailStreamState> {
            coinDetailStreamDataSource.maintainCoinDetailStream(targetSymbol).collect { throwable ->
                throw throwable
            }
        }

        emitAll(merge(detailStates, fatalErrors))
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
