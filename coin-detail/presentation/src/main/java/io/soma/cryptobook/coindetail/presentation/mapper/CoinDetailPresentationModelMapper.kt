package io.soma.cryptobook.coindetail.presentation.mapper

import io.soma.cryptobook.coindetail.domain.model.CoinCandleVO
import io.soma.cryptobook.coindetail.domain.model.CoinDetailVO
import io.soma.cryptobook.coindetail.domain.model.OrderBookLevelVO
import io.soma.cryptobook.coindetail.domain.model.OrderBookVO
import io.soma.cryptobook.coindetail.presentation.CandleUiModel
import io.soma.cryptobook.coindetail.presentation.CoinDetailContract
import io.soma.cryptobook.coindetail.presentation.OrderBookUiModel
import io.soma.cryptobook.coindetail.presentation.component.ORDER_BOOK_ROW_COUNT
import io.soma.cryptobook.coindetail.presentation.component.OrderBookEntry
import io.soma.cryptobook.coindetail.presentation.component.OrderBookRowUiModel
import io.soma.cryptobook.core.designsystem.util.Text
import io.soma.cryptobook.core.presentation.format.TickSizePriceFormatter
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * Maps CoinDetailVO (domain model) to CoinDetailUiState (presentation model)
 *
 * Responsibilities:
 * - Convert BigDecimal to formatted String for display
 * - Format prices with currency symbol and thousand separators
 * - Format volume with appropriate unit (B, M, K)
 * - Format price change text with sign and percentage
 */
class CoinDetailPresentationModelMapper @Inject constructor() {

    /**
     * Converts domain model to presentation model
     */
    fun toUiState(
        vo: CoinDetailVO,
        candles: List<CoinCandleVO>,
        orderBook: OrderBookVO?,
        imageUrl: String,
        isLoading: Boolean = false,
        errorMsg: Text? = null,
    ): CoinDetailContract.State = CoinDetailContract.State(
        symbol = vo.symbol,
        imageUrl = imageUrl,
        currentPrice = formatPrice(vo.currentPrice, vo.tickSize),
        priceChangeText = formatPriceChange(vo.priceChange, vo.priceChangePercent, vo.tickSize),
        priceChangePercent = vo.priceChangePercent,
        candles = candles.map { it.toUiModel() },
        high24h = formatPrice(vo.high24h, vo.tickSize),
        low24h = formatPrice(vo.low24h, vo.tickSize),
        volume24h = formatVolume(vo.volume24h),
        openPrice = formatPrice(vo.openPrice, vo.tickSize),
        tickSize = vo.tickSize,
        orderBook = toOrderBookUiModel(orderBook, vo.tickSize),
        isLoading = isLoading,
        errorMsg = errorMsg,
    )

    /**
     * Formats price as "$70,123.45"
     */
    private fun formatPrice(value: BigDecimal, tickSize: BigDecimal?): String =
        TickSizePriceFormatter.formatUsd(value, tickSize)

    /**
     * Formats volume with appropriate unit
     * - >= 1B: "$25.4B"
     * - >= 1M: "$100.5M"
     * - >= 1K: "$500.2K"
     * - else: "$123.45"
     */
    private fun formatVolume(value: BigDecimal): String {
        val billion = BigDecimal("1000000000")
        val million = BigDecimal("1000000")
        val thousand = BigDecimal("1000")

        return when {
            value >= billion -> {
                val result = value.divide(billion, 1, RoundingMode.HALF_UP)
                "$${result}B"
            }

            value >= million -> {
                val result = value.divide(million, 1, RoundingMode.HALF_UP)
                "$${result}M"
            }

            value >= thousand -> {
                val result = value.divide(thousand, 1, RoundingMode.HALF_UP)
                "$${result}K"
            }

            else -> TickSizePriceFormatter.formatUsd(value, tickSize = null)
        }
    }

    /**
     * Formats price change as "+1,840.55 (+2.58%)" or "-1,840.55 (-2.58%)"
     */
    private fun formatPriceChange(
        priceChange: BigDecimal,
        priceChangePercent: Double,
        tickSize: BigDecimal?,
    ): String {
        val priceSign = if (priceChangePercent >= 0) "+" else "-"
        val percentSign = if (priceChangePercent >= 0) "+" else ""
        val priceChangeFormatted = TickSizePriceFormatter.format(priceChange.abs(), tickSize)
        val percentFormatted = String.format("%.2f", priceChangePercent)

        return "$priceSign$$priceChangeFormatted ($percentSign$percentFormatted%)"
    }

    private fun CoinCandleVO.toUiModel(): CandleUiModel = CandleUiModel(
        openTime = openTime,
        closeTime = closeTime,
        open = open,
        close = close,
        high = high,
        low = low,
    )

    private fun toOrderBookUiModel(
        orderBook: OrderBookVO?,
        tickSize: BigDecimal?,
    ): OrderBookUiModel {
        val tickSizeText = tickSize?.let { TickSizePriceFormatter.format(it, it) }.orEmpty()

        if (orderBook == null) {
            return OrderBookUiModel.EMPTY.copy(tickSizeText = tickSizeText)
        }

        val bids = orderBook.bids.take(ORDER_BOOK_ROW_COUNT)
        val asks = orderBook.asks.take(ORDER_BOOK_ROW_COUNT)
        val bidCumulative = cumulativeQuantities(bids)
        val askCumulative = cumulativeQuantities(asks)
        val bidTotal = bidCumulative.lastOrNull() ?: BigDecimal.ZERO
        val askTotal = askCumulative.lastOrNull() ?: BigDecimal.ZERO
        val maxCumulative = bidTotal.max(askTotal)

        val rows = List(ORDER_BOOK_ROW_COUNT) { index ->
            OrderBookRowUiModel(
                bid = bids.getOrNull(index)?.let { level ->
                    OrderBookEntry(
                        price = TickSizePriceFormatter.format(level.price, tickSize),
                        quantity = formatQuantity(level.quantity),
                        depthRatio = depthRatio(bidCumulative[index], maxCumulative),
                    )
                },
                ask = asks.getOrNull(index)?.let { level ->
                    OrderBookEntry(
                        price = TickSizePriceFormatter.format(level.price, tickSize),
                        quantity = formatQuantity(level.quantity),
                        depthRatio = depthRatio(askCumulative[index], maxCumulative),
                    )
                },
            )
        }

        val total = bidTotal + askTotal
        val bidRatio = if (total.signum() == 0) {
            0.5f
        } else {
            (bidTotal.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        }

        return OrderBookUiModel(
            rows = rows,
            bidPercentText = formatPercent(bidRatio * 100f),
            askPercentText = formatPercent((1f - bidRatio) * 100f),
            bidRatio = bidRatio,
            tickSizeText = tickSizeText,
        )
    }

    private fun cumulativeQuantities(levels: List<OrderBookLevelVO>): List<BigDecimal> {
        var running = BigDecimal.ZERO
        return levels.map { level ->
            running += level.quantity
            running
        }
    }

    private fun depthRatio(cumulative: BigDecimal, max: BigDecimal): Float {
        if (max.signum() == 0) return 0f
        return (cumulative.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    }

    private fun formatQuantity(value: BigDecimal): String =
        value.stripTrailingZeros().toPlainString()

    private fun formatPercent(value: Float): String = String.format("%.2f%%", value)
}
