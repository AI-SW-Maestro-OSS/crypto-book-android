package io.soma.cryptobook.core.designsystem.component.coinlist

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Data class for coin list item display
 *
 * @param symbol Full market symbol used as list key and click identifier (e.g. "BTCUSDT")
 * @param baseSymbol Base asset symbol (e.g. "BTC")
 * @param quoteSymbol Quote asset symbol (e.g. "USDT"); empty when unknown
 * @param imageUrl Coin image URL
 * @param price Formatted primary price string (e.g. "0.8934")
 * @param secondaryPrice Formatted secondary price string (e.g. "₩1,198.5"); null hides the line
 * @param volume Formatted trading volume string (e.g. "67.8M")
 * @param changePercent 24h change percentage value
 */
data class CoinListItemData(
    val symbol: String,
    val baseSymbol: String,
    val quoteSymbol: String,
    val imageUrl: String,
    val price: String,
    val secondaryPrice: String?,
    val volume: String,
    val changePercent: Double,
)

/**
 * Coin list table container component
 *
 * Figma element name: Container
 * Figma element type: Frame
 * Figma node-id: 1:13
 *
 * Displays:
 * - Scrollable list of coin items
 *
 * Dependencies:
 * - [CoinListItem]
 *
 * Layout:
 * - Full size with vertical scroll (LazyColumn)
 * - Padding: horizontal 16dp, vertical 7dp
 * - Gap between items: 8dp
 * - Items centered horizontally
 *
 * @param coins List of coin data to display
 * @param onCoinClick Callback when a coin item is clicked
 * @param modifier Optional modifier
 * @param lazyListState Hoisted scroll state so callers can observe scrolling (e.g. jank tracking)
 */
@Composable
fun CoinListTable(
    coins: List<CoinListItemData>,
    onCoinClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(
            items = coins,
            key = { it.symbol },
        ) { coin ->
            CoinListItem(
                baseSymbol = coin.baseSymbol,
                quoteSymbol = coin.quoteSymbol,
                imageUrl = coin.imageUrl,
                price = coin.price,
                secondaryPrice = coin.secondaryPrice,
                volume = coin.volume,
                changePercent = coin.changePercent,
                onClick = { onCoinClick(coin.symbol) },
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CoinListTablePreview() {
    val sampleCoins = listOf(
        CoinListItemData("BTCUSDT", "BTC", "USDT", "", "68,500.52", "₩91,910,000", "1.2B", 0.0),
        CoinListItemData("ETHUSDT", "ETH", "USDT", "", "3,500.25", "₩4,696,000", "850.4M", 1.75),
        CoinListItemData("BNBUSDT", "BNB", "USDT", "", "580.10", "₩778,000", "120.7M", -1.75),
        CoinListItemData("SOLUSDT", "SOL", "USDT", "", "145.30", "₩194,900", "67.8M", 0.0),
        CoinListItemData("XRPUSDT", "XRP", "USDT", "", "0.5234", "₩702.1", "45.3M", 2.50),
    )

    CoinListTable(
        coins = sampleCoins,
        onCoinClick = {},
    )
}
