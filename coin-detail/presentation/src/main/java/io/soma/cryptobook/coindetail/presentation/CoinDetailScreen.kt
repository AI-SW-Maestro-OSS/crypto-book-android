package io.soma.cryptobook.coindetail.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.soma.cryptobook.coindetail.presentation.component.MetricCardGridContainer
import io.soma.cryptobook.coindetail.presentation.component.PriceChange
import io.soma.cryptobook.coindetail.presentation.component.PriceChangeType
import io.soma.cryptobook.core.designsystem.theme.ScreenBackground
import io.soma.cryptobook.core.designsystem.theme.component.CbDetailTopAppBar

@Composable
fun CoinDetailRoute(modifier: Modifier = Modifier, viewModel: CoinDetailViewModel) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        CbDetailTopAppBar(
            onSearchClick = { },
            title = uiState.symbol,
            onBackClick = { },
            onFavoriteClick = { },
            modifier = modifier,
        )
        CoinDetailScreen(
            state = uiState,
            onEvent = viewModel::handleEvent,
            modifier = modifier,
        )
    }
}

@Composable
internal fun CoinDetailScreen(
    state: CoinDetailUiState,
    onEvent: (CoinDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
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

@Composable
private fun CoinDetailContent(state: CoinDetailUiState, modifier: Modifier = Modifier) {
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
            price = state.currentPrice,
            priceChangeText = state.priceChangeText,
            priceChangeType = priceChangeType,
        )

        MetricCardGridContainer(
            high24h = state.high24h,
            low24h = state.low24h,
            volume24h = state.volume24h,
            openPrice = state.openPrice,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CoinDetailScreenPreview() {
    CoinDetailScreen(
        state = CoinDetailUiState(
            symbol = "BTCUSDT",
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
        modifier = Modifier.background(ScreenBackground),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CoinDetailScreenLoadingPreview() {
    CoinDetailScreen(
        state = CoinDetailUiState(isLoading = true),
        onEvent = {},
        modifier = Modifier.background(ScreenBackground),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CoinDetailScreenErrorPreview() {
    CoinDetailScreen(
        state = CoinDetailUiState(
            isLoading = false,
            errorMsg = "Network error occurred",
        ),
        onEvent = {},
        modifier = Modifier.background(ScreenBackground),
    )
}
