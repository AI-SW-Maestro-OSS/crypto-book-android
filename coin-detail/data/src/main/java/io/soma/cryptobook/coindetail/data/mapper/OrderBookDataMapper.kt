package io.soma.cryptobook.coindetail.data.mapper

import io.soma.cryptobook.coindetail.data.model.BinanceSpotDepthDto
import io.soma.cryptobook.coindetail.data.orderbook.DepthDiff
import io.soma.cryptobook.coindetail.data.orderbook.DepthSnapshot
import io.soma.cryptobook.coindetail.domain.model.OrderBookLevelVO
import io.soma.cryptobook.core.network.market.WsDepthEventPayload

fun BinanceSpotDepthDto.toDepthSnapshot(): DepthSnapshot = DepthSnapshot(
    lastUpdateId = lastUpdateId,
    bids = bids.toOrderBookLevels(),
    asks = asks.toOrderBookLevels(),
)

fun WsDepthEventPayload.toDepthDiff(): DepthDiff = DepthDiff(
    firstUpdateId = firstUpdateId,
    finalUpdateId = finalUpdateId,
    bids = bids.toOrderBookLevels(),
    asks = asks.toOrderBookLevels(),
)

private fun List<List<String>>.toOrderBookLevels(): List<OrderBookLevelVO> = mapNotNull { entry ->
    val price = entry.getOrNull(0)?.toBigDecimalOrNull() ?: return@mapNotNull null
    val quantity = entry.getOrNull(1)?.toBigDecimalOrNull() ?: return@mapNotNull null
    OrderBookLevelVO(price = price, quantity = quantity)
}
