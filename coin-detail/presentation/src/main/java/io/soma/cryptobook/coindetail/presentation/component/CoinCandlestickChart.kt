package io.soma.cryptobook.coindetail.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.soma.cryptobook.coindetail.presentation.CandleUiModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberCandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.candlestickSeries

@Composable
fun CoinCandlestickChart(
    candles: List<CandleUiModel>,
    modifier: Modifier = Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val chart = rememberCartesianChart(rememberCandlestickCartesianLayer())

    LaunchedEffect(candles) {
        if (candles.isEmpty()) return@LaunchedEffect
        modelProducer.runTransaction {
            candlestickSeries(
                candles.indices.map { it.toFloat() },
                candles.map { it.open.toFloat() },
                candles.map { it.close.toFloat() },
                candles.map { it.low.toFloat() },
                candles.map { it.high.toFloat() },
            )
        }
    }

    if (candles.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("차트 로딩 중...")
        }
    } else {
        CartesianChartHost(
            chart = chart,
            modelProducer = modelProducer,
            modifier = modifier.fillMaxSize(),
        )
    }
}
