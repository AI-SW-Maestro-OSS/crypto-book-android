package io.soma.cryptobook.coindetail.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.helpingstar.kandle.FinancialChart
import io.github.helpingstar.kandle.data.FinancialChartModelProducer
import io.github.helpingstar.kandle.format.PriceFormatter
import io.github.helpingstar.kandle.format.TimeFormatter
import io.github.helpingstar.kandle.material3.MaterialChartDefaults
import io.github.helpingstar.kandle.state.rememberFinancialChartState
import io.github.helpingstar.kandle.theme.CandleColorConvention
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.presentation.format.TickSizePriceFormatter
import java.math.BigDecimal

// Bar spacing at zoom = 1.0; tuned so a typical phone width shows ~24 candles initially.
private const val DEFAULT_BAR_SPACING_DP = 14f

private const val PRICE_PANEL_WEIGHT = 0.7f
private const val VOLUME_PANEL_WEIGHT = 0.3f

/**
 * Price + volume candlestick chart backed by Kandle. The [producer] is owned and fed incrementally
 * by [io.soma.cryptobook.coindetail.presentation.CoinDetailViewModel] (vico/Kandle best practice),
 * so it survives configuration changes; this composable only renders it.
 *
 * The latest bar stays flush right with new-bar auto-follow on (Kandle defaults), the Y axis
 * auto-fits the visible range, and long-press shows the built-in OHLCV crosshair/tooltip. Prices are
 * formatted with the symbol's tick size; the X axis adapts its precision to the visible range.
 */
@Composable
fun CoinCandlestickChart(
    producer: FinancialChartModelProducer,
    tickSize: BigDecimal?,
    modifier: Modifier = Modifier,
) {
    val state = rememberFinancialChartState(defaultBarSpacingDp = DEFAULT_BAR_SPACING_DP)
    val priceFormatter = remember(tickSize) {
        PriceFormatter.custom { value -> TickSizePriceFormatter.format(value, tickSize) }
    }
    val timeFormatter = remember { TimeFormatter.adaptive() }

    FinancialChart(
        modelProducer = producer,
        modifier = modifier,
        state = state,
        theme = MaterialChartDefaults.theme(colorConvention = CandleColorConvention.Western),
        priceFormatter = priceFormatter,
        timeFormatter = timeFormatter,
        emptyContent = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(CryptoString.crypto_coin_detail_chart_loading))
            }
        },
    ) {
        priceLayer(heightWeight = PRICE_PANEL_WEIGHT)
        volumeLayer(heightWeight = VOLUME_PANEL_WEIGHT)
    }
}
