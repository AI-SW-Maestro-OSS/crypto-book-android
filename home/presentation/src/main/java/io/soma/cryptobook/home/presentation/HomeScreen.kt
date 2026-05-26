package io.soma.cryptobook.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.component.appbar.CbMediumTopAppBar
import io.soma.cryptobook.core.designsystem.theme.component.button.CbStandardIconButton
import io.soma.cryptobook.core.designsystem.theme.component.scaffold.CbScaffold
import io.soma.cryptobook.core.designsystem.theme.resource.CbDrawable
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme
import io.soma.cryptobook.core.presentation.format.TickSizePriceFormatter
import io.soma.cryptobook.home.presentation.component.coinlist.CoinListItemData
import io.soma.cryptobook.home.presentation.component.coinlist.CoinListTable
import io.soma.cryptobook.home.presentation.component.sortheader.SortDirection
import io.soma.cryptobook.home.presentation.component.sortheader.SortHeader
import java.math.BigDecimal

@Composable
fun HomeRoute(modifier: Modifier = Modifier, viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        state = uiState,
        onEvent = viewModel::handleEvent,
        modifier = modifier,
    )
}

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    CbScaffold(
        modifier = modifier,
        topBar = {
            CbMediumTopAppBar(
                title = "Crypto-Book-Android",
                actions = {
                    CbStandardIconButton(
                        vectorIconRes = CbDrawable.ic_search,
                        contentDescription = "search",
                        onClick = { onEvent(HomeEvent.SearchIconClick) },
                        modifier = Modifier,
                    )
                },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CbTheme.colorScheme.background.secondary),
        ) {
            state.realtimeStatusMessage?.let { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF3CD))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(text = msg, color = Color(0xFF8A6D3B))
                }
            }

            // Error message
            state.errorMsg?.let { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Red.copy(alpha = 0.1f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(text = msg, color = Color.Red)
                }
            }

            // Sort Header (TODO: 정렬 기능 구현)
            SortHeader(
                symbolSort = SortDirection.Asc,
                priceSort = SortDirection.Desc,
                changeSort = SortDirection.None,
                volumeSort = SortDirection.None,
                onSymbolClick = { /* TODO: 정렬 기능 구현 */ },
                onPriceClick = { /* TODO: 정렬 기능 구현 */ },
                onChangeClick = { /* TODO: 정렬 기능 구현 */ },
                onVolumeClick = { /* TODO: 정렬 기능 구현 */ },
            )

            // Coin List
            Box(modifier = Modifier.fillMaxSize()) {
                if (state.coins.isNotEmpty()) {
                    CoinListTable(
                        coins = state.coins.map { it.toCoinListItemData() },
                        onCoinClick = { symbol -> onEvent(HomeEvent.OnCoinClicked(symbol)) },
                    )
                }

                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

/**
 * Convert CoinItem to CoinListItemData
 * TODO: API에서 name 받아오기
 */
private fun CoinItem.toCoinListItemData() = CoinListItemData(
    symbol = symbol,
    name = symbol.removeSuffix("USDT"),
    imageUrl = imageUrl,
    price = TickSizePriceFormatter.formatUsd(price, tickSize),
    changePercent = priceChangePercentage24h,
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    val sampleCoins = listOf(
        CoinItem("BTCUSDT", "", BigDecimal("68500.52"), 2.35),
        CoinItem("ETHUSDT", "", BigDecimal("3500.25"), -1.75),
        CoinItem("BNBUSDT", "", BigDecimal("580.10"), 0.0),
        CoinItem("SOLUSDT", "", BigDecimal("145.30"), 5.20),
        CoinItem("XRPUSDT", "", BigDecimal("0.52"), -0.85),
    )

    HomeScreen(
        state = HomeUiState(coins = sampleCoins),
        onEvent = {},
        modifier = Modifier.background(CbTheme.colorScheme.background.primary),
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    HomeScreen(
        state = HomeUiState(isLoading = true),
        onEvent = {},
        modifier = Modifier.background(CbTheme.colorScheme.background.primary),
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenErrorPreview() {
    HomeScreen(
        state = HomeUiState(errorMsg = stringResource(CryptoString.cb_error_network)),
        onEvent = {},
        modifier = Modifier.background(CbTheme.colorScheme.background.primary),
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenRealtimeWarningPreview() {
    HomeScreen(
        state = HomeUiState(
            realtimeStatusMessage = stringResource(CryptoString.cb_realtime_recovering),
        ),
        onEvent = {},
        modifier = Modifier.background(CbTheme.colorScheme.background.primary),
    )
}
