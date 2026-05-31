package io.soma.cryptobook.coindetail.domain.model

import java.math.BigDecimal

/**
 * Aggregated order book for a symbol, ordered best-first.
 *
 * @property bids Buy levels, highest price first.
 * @property asks Sell levels, lowest price first.
 */
data class OrderBookVO(val bids: List<OrderBookLevelVO>, val asks: List<OrderBookLevelVO>)

data class OrderBookLevelVO(val price: BigDecimal, val quantity: BigDecimal)
