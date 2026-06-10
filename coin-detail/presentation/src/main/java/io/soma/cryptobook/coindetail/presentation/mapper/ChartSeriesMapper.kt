package io.soma.cryptobook.coindetail.presentation.mapper

import io.github.helpingstar.kandle.data.Candle
import io.github.helpingstar.kandle.data.VolumeBar
import io.soma.cryptobook.coindetail.domain.model.CoinCandleVO

/**
 * The chart's price + volume series, sanitized for [io.github.helpingstar.kandle.FinancialChart].
 *
 * Kandle validation is strict and always on: timestamps must be strictly increasing with no
 * duplicates, every value finite, and `low <= min(open, close)`, `high >= max(open, close)`. Binance
 * klines can violate these (the forming bar is re-sent under the same open time; missing fields fall
 * back to 0.0), so [toChartSeries] sorts, de-duplicates by open time keeping the latest, drops
 * non-finite bars, and clamps OHLC before building the [Candle]s / [VolumeBar]s.
 */
data class ChartSeries(val candles: List<Candle>, val volumes: List<VolumeBar>)

/** Converts a raw candle stream into a validation-safe [ChartSeries]. */
fun List<CoinCandleVO>.toChartSeries(): ChartSeries {
    val sanitized = this
        .filter {
            it.open.isFinite() && it.close.isFinite() && it.high.isFinite() && it.low.isFinite()
        }
        .sortedBy { it.openTime }
        .groupBy { it.openTime }
        .values
        .map { sameOpenTime -> sameOpenTime.last().normalizedOhlc() }

    return ChartSeries(
        candles = sanitized.map { candle ->
            Candle(
                timestamp = candle.openTime,
                open = candle.open,
                high = candle.high,
                low = candle.low,
                close = candle.close,
            )
        },
        volumes = sanitized.map { candle ->
            VolumeBar(
                timestamp = candle.openTime,
                value = if (candle.volume.isFinite()) candle.volume else 0.0,
            )
        },
    )
}

private fun CoinCandleVO.normalizedOhlc(): CoinCandleVO {
    val normalizedLow = minOf(low, open, close, high)
    val normalizedHigh = maxOf(high, open, close, normalizedLow)
    return copy(low = normalizedLow, high = normalizedHigh)
}
