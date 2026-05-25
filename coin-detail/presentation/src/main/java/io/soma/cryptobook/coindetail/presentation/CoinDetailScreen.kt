package io.soma.cryptobook.coindetail.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.soma.cryptobook.coindetail.presentation.CoinDetailContract.Effect
import io.soma.cryptobook.coindetail.presentation.CoinDetailContract.Event
import io.soma.cryptobook.coindetail.presentation.CoinDetailContract.State
import io.soma.cryptobook.coindetail.presentation.CoinDetailContract.ViewModel
import io.soma.cryptobook.coindetail.presentation.component.CoinCandlestickChart
import io.soma.cryptobook.coindetail.presentation.component.MetricCardGridContainer
import io.soma.cryptobook.coindetail.presentation.component.PriceChange
import io.soma.cryptobook.coindetail.presentation.component.PriceChangeType
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.component.appbar.CbMediumTopAppBar
import io.soma.cryptobook.core.designsystem.theme.component.appbar.NavigationIcon
import io.soma.cryptobook.core.designsystem.theme.component.button.CbStandardIconButton
import io.soma.cryptobook.core.designsystem.theme.component.scaffold.CbScaffold
import io.soma.cryptobook.core.designsystem.theme.resource.CbDrawable
import io.soma.cryptobook.core.presentation.mvi.observe

@Composable
fun CoinDetailRoute(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: ViewModel) {
    val (state, dispatch) = viewModel.observe { effect ->
        when (effect) {
            Effect.NavigateBack -> onBack()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                dispatch(Event.OnScreenStarted)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    CoinDetailScreen(
        state = state.value,
        onEvent = dispatch,
        modifier = modifier,
    )
}

@Composable
internal fun CoinDetailScreen(
    state: State,
    onEvent: (Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    CbScaffold(
        modifier = modifier,
        topBar = {
            CbMediumTopAppBar(
                title = state.symbol,
                navigationIcon = NavigationIcon(
                    navigationIcon = painterResource(id = CbDrawable.ic_arrow_back),
                    navigationIconContentDescription = stringResource(
                        CryptoString.cb_coin_detail_back_cd,
                    ),
                    onNavigationIconClick = { onEvent(Event.OnBackClicked) },
                ),
                actions = {
                    CbStandardIconButton(
                        vectorIconRes = CbDrawable.ic_favorite,
                        contentDescription = stringResource(
                            CryptoString.cb_coin_detail_favorite_cd,
                        ),
                        onClick = { },
                        modifier = Modifier,
                    )
                },
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            state.realtimeStatusMessage?.let { message ->
                Text(
                    text = message,
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
                        text = state.errorMsg,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                else -> {
                    CoinDetailContent(state = state)
                }
            }
        }
    }
}

@Composable
private fun CoinDetailContent(state: State, modifier: Modifier = Modifier) {
    val priceChangeType = when {
        state.priceChangePercent > 0 -> PriceChangeType.Up
        state.priceChangePercent < 0 -> PriceChangeType.Down
        else -> PriceChangeType.Flat
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        modifier = modifier.padding(16.dp),
    ) {
        PriceChange(
            imageUrl = state.imageUrl,
            price = state.currentPrice,
            priceChangeText = state.priceChangeText,
            priceChangeType = priceChangeType,
        )

        CoinCandlestickChart(
            candles = state.candles,
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
}

@Preview(showBackground = true)
@Composable
private fun CoinDetailScreenPreview() {
    CoinDetailScreen(
        state = State(
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
        onEvent = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun CoinDetailScreenLoadingPreview() {
    CoinDetailScreen(
        state = State(isLoading = true),
        onEvent = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun CoinDetailScreenErrorPreview() {
    CoinDetailScreen(
        state = State(
            isLoading = false,
            errorMsg = stringResource(CryptoString.cb_coin_detail_connection_error_state),
        ),
        onEvent = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun CoinDetailScreenRealtimeWarningPreview() {
    CoinDetailScreen(
        state = State(
            isLoading = false,
            realtimeStatusMessage = stringResource(CryptoString.cb_realtime_recovering),
        ),
        onEvent = {},
    )
}
