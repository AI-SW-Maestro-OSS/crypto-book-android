package io.soma.cryptobook.core.designsystem.component.coinlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * Combined sort header + coin list table.
 *
 * Shared between the Home and Watchlist screens, which display an identical
 * sortable coin list. Scroll state is hoisted via [lazyListState] so callers can
 * attach behavior such as scroll jank tracking.
 *
 * @param emptyContent Shown when [coins] is empty and not loading.
 */
@Composable
fun CryptoCoinListSection(
    coins: List<CoinListItemData>,
    isLoading: Boolean,
    symbolSort: SortDirection,
    volumeSort: SortDirection,
    priceSort: SortDirection,
    changeSort: SortDirection,
    onSymbolClick: () -> Unit,
    onVolumeClick: () -> Unit,
    onPriceClick: () -> Unit,
    onChangeClick: () -> Unit,
    onCoinClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    emptyContent: @Composable () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxSize()) {
        SortHeader(
            symbolSort = symbolSort,
            priceSort = priceSort,
            changeSort = changeSort,
            volumeSort = volumeSort,
            onSymbolClick = onSymbolClick,
            onPriceClick = onPriceClick,
            onChangeClick = onChangeClick,
            onVolumeClick = onVolumeClick,
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                coins.isNotEmpty() -> CoinListTable(
                    coins = coins,
                    onCoinClick = onCoinClick,
                    lazyListState = lazyListState,
                )

                !isLoading -> emptyContent()
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CryptoCoinListSectionPreview() {
    val sampleCoins = listOf(
        CoinListItemData("BTCUSDT", "BTC", "USDT", "", "68,500.52", "₩91,910,000", "1.2B", 2.35),
        CoinListItemData("ETHUSDT", "ETH", "USDT", "", "3,500.25", "₩4,696,000", "850.4M", -1.75),
        CoinListItemData("SOLUSDT", "SOL", "USDT", "", "145.30", "₩194,900", "67.8M", 5.20),
    )

    CryptoCoinListSection(
        coins = sampleCoins,
        isLoading = false,
        symbolSort = SortDirection.None,
        volumeSort = SortDirection.None,
        priceSort = SortDirection.Desc,
        changeSort = SortDirection.None,
        onSymbolClick = {},
        onVolumeClick = {},
        onPriceClick = {},
        onChangeClick = {},
        onCoinClick = {},
    )
}
