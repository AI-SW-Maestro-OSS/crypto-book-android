package io.soma.cryptobook.watchlist.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
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
import io.soma.cryptobook.core.designsystem.theme.component.coinlist.CbCoinListSection
import io.soma.cryptobook.core.designsystem.theme.component.coinlist.CoinListItemData
import io.soma.cryptobook.core.designsystem.theme.component.coinlist.SortDirection
import io.soma.cryptobook.core.designsystem.theme.component.scaffold.CbScaffold
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme
import io.soma.cryptobook.core.domain.model.CoinSortColumn
import io.soma.cryptobook.core.domain.model.CoinSortDirection
import io.soma.cryptobook.core.presentation.format.TickSizePriceFormatter
import io.soma.cryptobook.core.presentation.jank.TrackScrollJank
import java.math.BigDecimal

@Composable
fun WatchlistRoute(modifier: Modifier = Modifier, viewModel: WatchlistViewModel = hiltViewModel()) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    WatchlistScreen(
        state = uiState,
        onEvent = viewModel::handleEvent,
        modifier = modifier,
    )
}

@Composable
internal fun WatchlistScreen(
    state: WatchlistUiState,
    onEvent: (WatchlistEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    TrackScrollJank(scrollableState = lazyListState, stateName = "watchlist:coinList")

    CbScaffold(
        modifier = modifier,
        topBar = {
            CbMediumTopAppBar(
                title = stringResource(CryptoString.cb_top_level_watchlist),
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
                    Text(text = msg(), color = Color(0xFF8A6D3B))
                }
            }

            state.errorMsg?.let { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Red.copy(alpha = 0.1f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(text = msg(), color = Color.Red)
                }
            }

            CbCoinListSection(
                coins = state.coins.map { it.toCoinListItemData() },
                isLoading = state.isLoading,
                symbolSort = state.sortDirectionFor(CoinSortColumn.SYMBOL),
                volumeSort = state.sortDirectionFor(CoinSortColumn.VOLUME),
                priceSort = state.sortDirectionFor(CoinSortColumn.PRICE),
                changeSort = state.sortDirectionFor(CoinSortColumn.CHANGE),
                onSymbolClick = { onEvent(WatchlistEvent.OnSortClick(CoinSortColumn.SYMBOL)) },
                onVolumeClick = { onEvent(WatchlistEvent.OnSortClick(CoinSortColumn.VOLUME)) },
                onPriceClick = { onEvent(WatchlistEvent.OnSortClick(CoinSortColumn.PRICE)) },
                onChangeClick = { onEvent(WatchlistEvent.OnSortClick(CoinSortColumn.CHANGE)) },
                onCoinClick = { symbol -> onEvent(WatchlistEvent.OnCoinClicked(symbol)) },
                lazyListState = lazyListState,
                emptyContent = { WatchlistEmpty() },
            )
        }
    }
}

@Composable
private fun WatchlistEmpty(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(CryptoString.cb_watchlist_empty),
            color = CbTheme.colorScheme.text.secondary,
        )
    }
}

private fun WatchlistUiState.sortDirectionFor(column: CoinSortColumn): SortDirection =
    if (sortColumn != column) {
        SortDirection.None
    } else {
        when (sortDirection) {
            CoinSortDirection.NONE -> SortDirection.None
            CoinSortDirection.ASC -> SortDirection.Asc
            CoinSortDirection.DESC -> SortDirection.Desc
        }
    }

private fun CoinItem.toCoinListItemData() = CoinListItemData(
    symbol = symbol,
    name = symbol.removeSuffix("USDT"),
    imageUrl = imageUrl,
    price = TickSizePriceFormatter.formatUsd(price, tickSize),
    changePercent = priceChangePercentage24h,
)

@Preview(showBackground = true)
@Composable
private fun WatchlistScreenPreview() {
    val sampleCoins = listOf(
        CoinItem("BTCUSDT", "", BigDecimal("68500.52"), 2.35),
        CoinItem("ETHUSDT", "", BigDecimal("3500.25"), -1.75),
    )

    WatchlistScreen(
        state = WatchlistUiState(coins = sampleCoins),
        onEvent = {},
        modifier = Modifier.background(CbTheme.colorScheme.background.primary),
    )
}

@Preview(showBackground = true)
@Composable
private fun WatchlistScreenEmptyPreview() {
    WatchlistScreen(
        state = WatchlistUiState(isLoading = false),
        onEvent = {},
        modifier = Modifier.background(CbTheme.colorScheme.background.primary),
    )
}
