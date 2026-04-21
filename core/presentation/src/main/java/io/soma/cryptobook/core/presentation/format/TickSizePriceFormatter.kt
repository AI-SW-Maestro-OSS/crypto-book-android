package io.soma.cryptobook.core.presentation.format

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

object TickSizePriceFormatter {
    fun formatUsd(value: BigDecimal, tickSize: BigDecimal?): String =
        "$${format(value = value, tickSize = tickSize)}"

    fun format(value: Double, tickSize: BigDecimal?): String =
        format(value = BigDecimal.valueOf(value), tickSize = tickSize)

    fun format(value: BigDecimal, tickSize: BigDecimal?): String {
        val validTickSize = tickSize?.takeIf { it.signum() > 0 }
        val scale = validTickSize?.displayScale() ?: DEFAULT_SCALE
        val rounded = if (validTickSize == null) {
            value.setScale(DEFAULT_SCALE, RoundingMode.HALF_UP)
        } else {
            value.roundToTick(validTickSize, scale)
        }

        return decimalFormat(scale).format(rounded)
    }

    private fun BigDecimal.roundToTick(tickSize: BigDecimal, scale: Int): BigDecimal {
        val tickCount = divide(tickSize, 0, RoundingMode.HALF_UP)
        return tickCount
            .multiply(tickSize)
            .setScale(scale, RoundingMode.HALF_UP)
    }

    private fun BigDecimal.displayScale(): Int = stripTrailingZeros()
        .scale()
        .coerceAtLeast(0)

    private fun decimalFormat(scale: Int): DecimalFormat {
        val pattern = if (scale == 0) {
            "#,##0"
        } else {
            "#,##0.${"0".repeat(scale)}"
        }
        return DecimalFormat(pattern).apply {
            roundingMode = RoundingMode.HALF_UP
        }
    }

    private const val DEFAULT_SCALE = 2
}
