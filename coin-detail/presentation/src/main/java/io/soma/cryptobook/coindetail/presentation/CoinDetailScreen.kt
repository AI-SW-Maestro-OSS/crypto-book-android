package io.soma.cryptobook.coindetail.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.helpingstar.kandle.data.FinancialChartModelProducer
import io.soma.cryptobook.coindetail.presentation.component.CoinCandlestickChart
import io.soma.cryptobook.coindetail.presentation.component.MetricCardGridContainer
import io.soma.cryptobook.coindetail.presentation.component.OrderBookSection
import io.soma.cryptobook.coindetail.presentation.component.PriceChange
import io.soma.cryptobook.coindetail.presentation.component.PriceChangeType
import io.soma.cryptobook.core.designsystem.component.appbar.CryptoMediumTopAppBar
import io.soma.cryptobook.core.designsystem.component.appbar.NavigationIcon
import io.soma.cryptobook.core.designsystem.component.button.CryptoStandardIconButton
import io.soma.cryptobook.core.designsystem.component.scaffold.CryptoScaffold
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.resource.CryptoDrawable
import io.soma.cryptobook.core.designsystem.theme.theme.CryptoTheme
import io.soma.cryptobook.core.designsystem.util.asText
import io.soma.cryptobook.core.ui.EventsEffect

/**
 * The Coin Detail screen.
 *
 * Navigation 3 does not surface route arguments through [androidx.lifecycle.SavedStateHandle], so
 * [coinName] is handed to the ViewModel through its assisted factory instead.
 */
@Composable
fun CoinDetailScreen(
    coinName: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CoinDetailViewModel = hiltViewModel(
        creationCallback = { factory: CoinDetailViewModel.Factory -> factory.create(coinName) },
    ),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current

    EventsEffect(viewModel = viewModel) { event ->
        when (event) {
            CoinDetailEvent.NavigateBack -> onNavigateBack()

            is CoinDetailEvent.ShowToast -> {
                Toast
                    .makeText(
                        context,
                        event.text.toString(context.resources),
                        Toast.LENGTH_SHORT,
                    )
                    .show()
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.trySendAction(CoinDetailAction.ScreenStart)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    CryptoScaffold(
        modifier = modifier,
        topBar = {
            CryptoMediumTopAppBar(
                title = state.symbol,
                navigationIcon = NavigationIcon(
                    navigationIcon = painterResource(id = CryptoDrawable.ic_arrow_back),
                    navigationIconContentDescription = stringResource(
                        CryptoString.crypto_coin_detail_back_cd,
                    ),
                    onNavigationIconClick = {
                        viewModel.trySendAction(CoinDetailAction.BackClick)
                    },
                ),
                actions = {
                    CryptoStandardIconButton(
                        vectorIconRes = if (state.isWatchlisted) {
                            CryptoDrawable.ic_watchlist_filled
                        } else {
                            CryptoDrawable.ic_watchlist
                        },
                        contentDescription = stringResource(
                            CryptoString.crypto_coin_detail_favorite_cd,
                        ),
                        onClick = { viewModel.trySendAction(CoinDetailAction.FavoriteClick) },
                        modifier = Modifier,
                    )
                },
            )
        },
    ) {
        CoinDetailScreenContent(
            state = state,
            chartProducer = viewModel.chartProducer,
        )
    }
}

@Composable
internal fun CoinDetailScreenContent(
    state: CoinDetailState,
    chartProducer: FinancialChartModelProducer,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        state.realtimeStatusMessage?.let { message ->
            Text(
                text = message(),
                color = Color(0xFF8A6D3B),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF3CD))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        when {
            state.isLoading -> {
                CircularProgressIndicator()
            }

            state.errorMsg != null -> {
                Text(
                    text = state.errorMsg(),
                    color = CryptoTheme.colorScheme.status.error,
                )
            }

            else -> {
                CoinDetailBody(
                    state = state,
                    chartProducer = chartProducer,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CoinDetailBody(
    state: CoinDetailState,
    chartProducer: FinancialChartModelProducer,
    modifier: Modifier = Modifier,
) {
    val priceChangeType = when {
        state.priceChangePercent > 0 -> PriceChangeType.Up
        state.priceChangePercent < 0 -> PriceChangeType.Down
        else -> PriceChangeType.Flat
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
        ) {
            PriceChange(
                imageUrl = state.imageUrl,
                price = state.currentPrice,
                priceChangeText = state.priceChangeText,
                priceChangeType = priceChangeType,
            )

            CoinCandlestickChart(
                producer = chartProducer,
                tickSize = state.tickSize,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color.White),
            )

            MetricCardGridContainer(
                high24h = state.high24h,
                low24h = state.low24h,
                volume24h = state.volume24h,
                openPrice = state.openPrice,
            )
        }

        OrderBookSection(
            tickSize = state.orderBook.tickSizeText,
            bidPercentText = state.orderBook.bidPercentText,
            askPercentText = state.orderBook.askPercentText,
            bidRatio = state.orderBook.bidRatio,
            rows = state.orderBook.rows,
            onTickSizeClick = {},
        )

        Spacer(modifier = Modifier.height(height = 16.dp))
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Preview(showBackground = true)
@Composable
private fun CoinDetailScreenContentPreview() {
    CoinDetailScreenContent(
        state = CoinDetailState(
            symbol = "BTCUSDT",
            imageUrl = "",
            currentPrice = "$73,500.89",
            priceChangeText = "+$1,840.55 (+2.58%)",
            priceChangePercent = 2.58,
            high24h = "$73,800.00",
            low24h = "$68,200.00",
            volume24h = "$100.0M",
            openPrice = "$71,660.34",
            isLoading = false,
        ),
        chartProducer = remember { FinancialChartModelProducer() },
    )
}

@Preview(showBackground = true)
@Composable
private fun CoinDetailScreenContentLoadingPreview() {
    CoinDetailScreenContent(
        state = CoinDetailState(isLoading = true),
        chartProducer = remember { FinancialChartModelProducer() },
    )
}

@Preview(showBackground = true)
@Composable
private fun CoinDetailScreenContentErrorPreview() {
    CoinDetailScreenContent(
        state = CoinDetailState(
            isLoading = false,
            errorMsg = CryptoString.crypto_coin_detail_connection_error_state.asText(),
        ),
        chartProducer = remember { FinancialChartModelProducer() },
    )
}

@Preview(showBackground = true)
@Composable
private fun CoinDetailScreenContentRealtimeWarningPreview() {
    CoinDetailScreenContent(
        state = CoinDetailState(
            isLoading = false,
            realtimeStatusMessage = CryptoString.crypto_realtime_recovering.asText(),
        ),
        chartProducer = remember { FinancialChartModelProducer() },
    )
}
