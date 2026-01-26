package io.soma.cryptobook.coindetail.presentation.mapper

import io.soma.cryptobook.coindetail.domain.model.CoinDetailVO
import io.soma.cryptobook.coindetail.presentation.CoinDetailUiState
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
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
        isLoading: Boolean = false,
        errorMsg: String? = null,
    ): CoinDetailUiState {
        return CoinDetailUiState(
            symbol = vo.symbol,
            currentPrice = formatPrice(vo.currentPrice),
            priceChangeText = formatPriceChange(vo.priceChange, vo.priceChangePercent),
            priceChangePercent = vo.priceChangePercent,
            high24h = formatPrice(vo.high24h),
            low24h = formatPrice(vo.low24h),
            volume24h = formatVolume(vo.volume24h),
            openPrice = formatPrice(vo.openPrice),
            isLoading = isLoading,
            errorMsg = errorMsg,
        )
    }

    /**
     * Formats price as "$70,123.45"
     */
    private fun formatPrice(value: BigDecimal): String {
        val formatter = DecimalFormat("#,##0.00")
        val rounded = value.setScale(2, RoundingMode.HALF_UP)
        return "$${formatter.format(rounded)}"
    }

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
            else -> formatPrice(value)
        }
    }

    /**
     * Formats price change as "+1,840.55 (+2.58%)" or "-1,840.55 (-2.58%)"
     */
    private fun formatPriceChange(priceChange: BigDecimal, priceChangePercent: Double): String {
        val formatter = DecimalFormat("#,##0.00")
        val rounded = priceChange.setScale(2, RoundingMode.HALF_UP)
        val sign = if (priceChangePercent >= 0) "+" else ""

        val priceChangeFormatted = formatter.format(rounded.abs())
        val percentFormatted = String.format("%.2f", priceChangePercent)

        return "$sign$$priceChangeFormatted ($sign$percentFormatted%)"
    }
}
