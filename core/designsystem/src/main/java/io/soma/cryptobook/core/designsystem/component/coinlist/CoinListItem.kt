package io.soma.cryptobook.core.designsystem.component.coinlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.soma.cryptobook.core.designsystem.R
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.theme.CryptoTheme

/**
 * @param symbol Coin symbol (e.g., "BTCUSDT")
 * @param name Coin name (e.g., "Bitcoin")
 * @param imageUrl Coin image URL
 * @param price Formatted price string (e.g., "$68500.52")
 * @param changePercent Change percentage value
 * @param onClick Callback when item is clicked
 * @param modifier Optional modifier
 */
@Composable
fun CoinListItem(
    symbol: String,
    name: String,
    imageUrl: String,
    price: String,
    changePercent: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val changeColor = when {
        changePercent > 0 -> CryptoTheme.colorScheme.price.up
        changePercent < 0 -> CryptoTheme.colorScheme.price.down
        else -> CryptoTheme.colorScheme.price.flat
    }

    val changeText = when {
        changePercent >= 0 -> "+${String.format("%.2f", changePercent)}%"
        else -> "${String.format("%.2f", changePercent)}%"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CryptoTheme.colorScheme.background.secondary)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left section: Avatar + Symbol/Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Coin image
            AsyncImage(
                model = imageUrl,
                placeholder = painterResource(R.drawable.ic_coin_placeholder),
                error = painterResource(R.drawable.ic_coin_placeholder),
                fallback = painterResource(R.drawable.ic_coin_placeholder),
                contentDescription = stringResource(
                    CryptoString.crypto_home_coin_icon_cd_format,
                    name,
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(18.dp)),
            )

            Spacer(modifier = Modifier.width(11.dp))

            // Symbol and Name
            Column {
                Text(
                    text = symbol,
                    style = CryptoTheme.typography.titleMedium,
                    color = CryptoTheme.colorScheme.text.primary,
                )
                Text(
                    text = name,
                    style = CryptoTheme.typography.bodySmall,
                    color = CryptoTheme.colorScheme.text.secondary,
                )
            }
        }

        // Right section: Price + Change
        Column(
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = price,
                style = CryptoTheme.typography.labelMedium,
                color = CryptoTheme.colorScheme.text.primary,
            )
            Text(
                text = changeText,
                style = CryptoTheme.typography.labelMedium,
                color = changeColor,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CoinListItemUpPreview() {
    CoinListItem(
        symbol = "BTCUSDT",
        name = "Bitcoin",
        imageUrl = "",
        price = "$68500.52",
        changePercent = 1.75,
        onClick = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CoinListItemDownPreview() {
    CoinListItem(
        symbol = "BTCUSDT",
        name = "Bitcoin",
        imageUrl = "",
        price = "$68500.52",
        changePercent = -1.75,
        onClick = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CoinListItemFlatPreview() {
    CoinListItem(
        symbol = "BTCUSDT",
        name = "Bitcoin",
        imageUrl = "",
        price = "$68500.52",
        changePercent = 0.0,
        onClick = {},
    )
}
