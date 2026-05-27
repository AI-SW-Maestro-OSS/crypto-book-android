package io.soma.cryptobook.core.designsystem.theme.component.coinlist

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
fun CbCoinListSection(
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
private fun CbCoinListSectionPreview() {
    val sampleCoins = listOf(
        CoinListItemData("BTCUSDT", "Bitcoin", "", "$68500.52", 2.35),
        CoinListItemData("ETHUSDT", "Ethereum", "", "$3500.25", -1.75),
        CoinListItemData("SOLUSDT", "Solana", "", "$145.30", 5.20),
    )

    CbCoinListSection(
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
