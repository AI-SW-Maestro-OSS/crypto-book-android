package io.soma.cryptobook.coindetail.data.datasource

import io.soma.cryptobook.coindetail.data.mapper.toDepthSnapshot
import io.soma.cryptobook.coindetail.data.network.BinanceSpotDepthClient
import io.soma.cryptobook.coindetail.data.orderbook.DepthSnapshot
import javax.inject.Inject

class CoinDetailDepthSnapshotDataSource @Inject constructor(
    private val depthClient: BinanceSpotDepthClient,
) {
    suspend fun getDepth(symbol: String, limit: Int): DepthSnapshot =
        depthClient.getDepth(symbol = symbol.uppercase(), limit = limit).toDepthSnapshot()
}
