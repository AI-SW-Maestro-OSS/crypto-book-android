package io.soma.cryptobook.core.presentation.format

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Formats a trading volume into a compact form without a currency symbol.
 * - >= 1B: "25.4B"
 * - >= 1M: "67.8M"
 * - >= 1K: "500.2K"
 * - else: "123.45"
 */
object VolumeFormatter {
    private val BILLION = BigDecimal("1000000000")
    private val MILLION = BigDecimal("1000000")
    private val THOUSAND = BigDecimal("1000")

    fun format(value: BigDecimal): String = when {
        value >= BILLION -> "${value.divide(BILLION, 1, RoundingMode.HALF_UP)}B"
        value >= MILLION -> "${value.divide(MILLION, 1, RoundingMode.HALF_UP)}M"
        value >= THOUSAND -> "${value.divide(THOUSAND, 1, RoundingMode.HALF_UP)}K"
        else -> TickSizePriceFormatter.format(value, tickSize = null)
    }
}
