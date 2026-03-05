package io.soma.cryptobook.coindetail.data.datasource

import io.soma.cryptobook.coindetail.data.model.toCoinTickerDto
import io.soma.cryptobook.coindetail.data.network.BinanceFuturesTickerClient
import io.soma.cryptobook.core.data.model.CoinTickerDto
import javax.inject.Inject

class CoinDetailTickerSnapshotDataSource @Inject constructor(
    private val tickerClient: BinanceFuturesTickerClient,
) {
    suspend fun getTicker(symbol: String): CoinTickerDto = tickerClient.getTicker(
        symbol = symbol.uppercase(),
    ).toCoinTickerDto()
}
