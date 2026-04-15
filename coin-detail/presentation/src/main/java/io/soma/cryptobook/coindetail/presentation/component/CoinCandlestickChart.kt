package io.soma.cryptobook.coindetail.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.patrykandpatrick.vico.compose.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.candlestickSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.absoluteRelative
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberCandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import io.soma.cryptobook.coindetail.presentation.CandleUiModel

private const val START_AXIS_LABEL_COUNT = 5

@Composable
fun CoinCandlestickChart(candles: List<CandleUiModel>, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val scrollState = rememberVicoScrollState(
        initialScroll = Scroll.Absolute.End,
        autoScroll = Scroll.Absolute.End,
        autoScrollCondition = AutoScrollCondition.OnModelGrowth,
    )
    val zoomState = rememberVicoZoomState(
        initialZoom = remember {
            Zoom.max(Zoom.x(INITIAL_VISIBLE_CANDLES), Zoom.Content)
        },
    )
    val renderState = rememberCoinCandlestickChartRenderState(
        candles = candles,
        scrollState = scrollState,
        zoomState = zoomState,
    )
    val rangeProvider = remember { VisiblePriceRangeProvider() }
    val chart = rememberCartesianChart(
        rememberCandlestickCartesianLayer(
            candleProvider = CandlestickCartesianLayer.CandleProvider.absoluteRelative(),
            candleSpacing = CHART_CANDLE_SPACING,
            scaleCandleWicks = true,
            rangeProvider = rangeProvider,
        ),
        startAxis = VerticalAxis.rememberStart(
            valueFormatter = renderState.startAxisFormatter,
            itemPlacer = remember {
                VerticalAxis.ItemPlacer.count(count = { START_AXIS_LABEL_COUNT })
            },
        ),
        bottomAxis = HorizontalAxis.rememberBottom(
            valueFormatter = renderState.bottomAxisFormatter,
            itemPlacer = remember {
                HorizontalAxis.ItemPlacer.aligned(spacing = { BOTTOM_AXIS_LABEL_STEP })
            },
        ),
    )

    LaunchedEffect(renderState.modelInput) {
        modelProducer.runTransaction {
            renderState.modelInput.visiblePriceRange?.let { range ->
                extras { extraStore ->
                    extraStore[VisibleMinYKey] = range.min
                    extraStore[VisibleMaxYKey] = range.max
                }
            }

            if (renderState.modelInput.chartCandles.isNotEmpty()) {
                candlestickSeries(
                    x = renderState.modelInput.xValues,
                    opening = renderState.modelInput.openingValues,
                    closing = renderState.modelInput.closingValues,
                    low = renderState.modelInput.lowValues,
                    high = renderState.modelInput.highValues,
                )
            }
        }
    }

    CartesianChartHost(
        chart = chart,
        modelProducer = modelProducer,
        modifier = modifier,
        scrollState = scrollState,
        zoomState = zoomState,
        animationSpec = null,
        animateIn = false,
        placeholder = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = CHART_CANDLE_SPACING),
                contentAlignment = Alignment.Center,
            ) {
                Text("차트 로딩 중...")
            }
        },
    )
}
