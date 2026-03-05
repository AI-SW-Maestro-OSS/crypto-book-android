package io.soma.cryptobook.coindetail.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.VicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.VicoZoomState
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import io.soma.cryptobook.coindetail.presentation.CandleUiModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

internal const val INITIAL_VISIBLE_CANDLES = 24.0
internal const val BOTTOM_AXIS_LABEL_STEP = 6
internal val CHART_CANDLE_SPACING = 8.dp

private const val PRICE_RANGE_PADDING_RATIO = 0.05
private val DEFAULT_CANDLE_BODY_WIDTH = 8.dp

internal val VisibleMinYKey = ExtraStore.Key<Double>()
internal val VisibleMaxYKey = ExtraStore.Key<Double>()

internal data class CoinCandlestickChartModelInput(
    val chartCandles: List<CandleUiModel>,
    val xValues: List<Double>,
    val openingValues: List<Double>,
    val closingValues: List<Double>,
    val lowValues: List<Double>,
    val highValues: List<Double>,
    val visiblePriceRange: ChartPriceRange?,
)

internal data class CoinCandlestickChartRenderState(
    val modelInput: CoinCandlestickChartModelInput,
    val bottomAxisFormatter: CartesianValueFormatter,
    val startAxisFormatter: CartesianValueFormatter,
)

@Composable
internal fun rememberCoinCandlestickChartRenderState(
    candles: List<CandleUiModel>,
    scrollState: VicoScrollState,
    zoomState: VicoZoomState,
): CoinCandlestickChartRenderState {
    val density = LocalDensity.current
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val priceFormatter = remember { DecimalFormat("#,##0.########") }
    val candleBodyWidthPx = remember(density) {
        with(density) { DEFAULT_CANDLE_BODY_WIDTH.toPx() }
    }
    val candleSpacingPx = remember(density) {
        with(density) { CHART_CANDLE_SPACING.toPx() }
    }

    val modelInput by remember(
        candles,
        candleBodyWidthPx,
        candleSpacingPx,
        scrollState,
        zoomState,
    ) {
        derivedStateOf {
            val chartCandles = candles.toRenderableCandles()
            val visibleCandles = chartCandles.visibleSlice(
                scrollPx = scrollState.value,
                maxScrollPx = scrollState.maxValue,
                zoom = zoomState.value,
                candleBodyWidthPx = candleBodyWidthPx,
                candleSpacingPx = candleSpacingPx,
            )
            val visiblePriceRange = visibleCandles.toPriceRange() ?: chartCandles.toPriceRange()

            CoinCandlestickChartModelInput(
                chartCandles = chartCandles,
                xValues = chartCandles.indices.map(Int::toDouble),
                openingValues = chartCandles.map(CandleUiModel::open),
                closingValues = chartCandles.map(CandleUiModel::close),
                lowValues = chartCandles.map(CandleUiModel::low),
                highValues = chartCandles.map(CandleUiModel::high),
                visiblePriceRange = visiblePriceRange,
            )
        }
    }

    val bottomAxisFormatter = remember(modelInput.chartCandles, timeFormatter) {
        CartesianValueFormatter { _, value, _ ->
            val index = value.roundToInt()
            modelInput.chartCandles
                .getOrNull(index.coerceIn(0, modelInput.chartCandles.lastIndex.coerceAtLeast(0)))
                ?.let { candle -> timeFormatter.format(Date(candle.openTime)) }
                ?: " "
        }
    }
    val startAxisFormatter = remember(priceFormatter) {
        CartesianValueFormatter { _, value, _ ->
            priceFormatter.format(value)
        }
    }

    return remember(modelInput, bottomAxisFormatter, startAxisFormatter) {
        CoinCandlestickChartRenderState(
            modelInput = modelInput,
            bottomAxisFormatter = bottomAxisFormatter,
            startAxisFormatter = startAxisFormatter,
        )
    }
}

@Stable
internal class VisiblePriceRangeProvider(
    private val fallback: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
) : CartesianLayerRangeProvider {
    override fun getMinX(minX: Double, maxX: Double, extraStore: ExtraStore): Double =
        fallback.getMinX(minX, maxX, extraStore)

    override fun getMaxX(minX: Double, maxX: Double, extraStore: ExtraStore): Double =
        fallback.getMaxX(minX, maxX, extraStore)

    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double =
        extraStore.getOrNull(VisibleMinYKey) ?: fallback.getMinY(minY, maxY, extraStore)

    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double =
        extraStore.getOrNull(VisibleMaxYKey) ?: fallback.getMaxY(minY, maxY, extraStore)
}

private fun List<CandleUiModel>.toRenderableCandles(): List<CandleUiModel> = this
    .filter { candle ->
        candle.open.isFinite() &&
            candle.close.isFinite() &&
            candle.low.isFinite() &&
            candle.high.isFinite()
    }
    .sortedBy { it.openTime }
    .groupBy { it.openTime }
    .values
    .map { candlesAtSameTime ->
        candlesAtSameTime.last().normalized()
    }

private fun CandleUiModel.normalized(): CandleUiModel {
    val normalizedLow = minOf(low, open, close, high)
    val normalizedHigh = maxOf(high, open, close, normalizedLow)
    return copy(low = normalizedLow, high = normalizedHigh)
}

private fun List<CandleUiModel>.visibleSlice(
    scrollPx: Float,
    maxScrollPx: Float,
    zoom: Float,
    candleBodyWidthPx: Float,
    candleSpacingPx: Float,
): List<CandleUiModel> {
    if (isEmpty()) return emptyList()

    val baseXSpacingPx = candleBodyWidthPx + candleSpacingPx
    if (baseXSpacingPx <= 0f || zoom <= 0f) return this

    val scaledXSpacingPx = baseXSpacingPx * zoom
    val edgePaddingInX = (candleBodyWidthPx / 2f) / baseXSpacingPx
    val fullRangeStart = -edgePaddingInX
    val fullRangeLength = lastIndex.toDouble() + edgePaddingInX * 2.0
    val visibleRangeLength = (
        fullRangeLength - maxScrollPx.toDouble() / scaledXSpacingPx
    ).coerceIn(edgePaddingInX.toDouble(), fullRangeLength)
    val visibleRangeStart = fullRangeStart + scrollPx.toDouble() / scaledXSpacingPx
    val visibleRangeEnd = visibleRangeStart + visibleRangeLength

    val startIndex = (floor(visibleRangeStart).toInt() - 1).coerceAtLeast(0)
    val endExclusive = (ceil(visibleRangeEnd).toInt() + 2).coerceAtMost(size)

    if (startIndex >= endExclusive) return this

    return subList(startIndex, endExclusive)
}

internal data class ChartPriceRange(
    val min: Double,
    val max: Double,
)

private fun List<CandleUiModel>.toPriceRange(): ChartPriceRange? {
    if (isEmpty()) return null

    val minPrice = minOf { it.low }
    val maxPrice = maxOf { it.high }
    val spread = (maxPrice - minPrice).takeIf { it > 0.0 }
        ?: maxOf(kotlin.math.abs(maxPrice) * PRICE_RANGE_PADDING_RATIO, 1e-8)
    val padding = spread * PRICE_RANGE_PADDING_RATIO
    val paddedMin = (minPrice - padding).coerceAtLeast(0.0)
    val paddedMax = maxPrice + padding

    return ChartPriceRange(
        min = paddedMin,
        max = paddedMax,
    )
}

