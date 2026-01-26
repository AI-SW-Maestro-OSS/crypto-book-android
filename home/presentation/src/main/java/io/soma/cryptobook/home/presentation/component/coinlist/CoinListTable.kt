package io.soma.cryptobook.home.presentation.component.coinlist

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Data class for coin list item display
 */
data class CoinListItemData(
    val symbol: String,
    val name: String,
    val imageUrl: String,
    val price: String,
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
 */
@Composable
fun CoinListTable(
    coins: List<CoinListItemData>,
    onCoinClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(
            items = coins,
            key = { it.symbol },
        ) { coin ->
            CoinListItem(
                symbol = coin.symbol,
                name = coin.name,
                imageUrl = coin.imageUrl,
                price = coin.price,
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
        CoinListItemData("BTCUSDT", "Bitcoin", "", "$68500.52", 0.0),
        CoinListItemData("ETHUSDT", "Ethereum", "", "$3500.25", 1.75),
        CoinListItemData("BNBUSDT", "BNB", "", "$580.10", -1.75),
        CoinListItemData("SOLUSDT", "Solana", "", "$145.30", 0.0),
        CoinListItemData("XRPUSDT", "XRP", "", "$0.52", 2.50),
    )

    CoinListTable(
        coins = sampleCoins,
        onCoinClick = {},
    )
}
