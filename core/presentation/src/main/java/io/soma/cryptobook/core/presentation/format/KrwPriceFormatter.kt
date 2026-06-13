package io.soma.cryptobook.core.presentation.format

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * Formats a Korean Won value with the "₩" symbol and thousands separators.
 *
 * Sub-won values keep up to four fraction digits so cheap coins remain
 * readable; otherwise up to one fraction digit is shown (e.g. "₩1,198.5",
 * "₩92,475,000"). Trailing zero fractions are dropped.
 */
object KrwPriceFormatter {
    fun format(value: BigDecimal): String {
        val pattern = if (value.abs() < BigDecimal.ONE) "#,##0.####" else "#,##0.#"
        val formatted = DecimalFormat(pattern)
            .apply { roundingMode = RoundingMode.HALF_UP }
            .format(value)
        return "₩$formatted"
    }
}
