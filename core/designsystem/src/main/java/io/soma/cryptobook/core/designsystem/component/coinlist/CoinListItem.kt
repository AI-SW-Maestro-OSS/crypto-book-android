package io.soma.cryptobook.core.designsystem.component.coinlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.soma.cryptobook.core.designsystem.R
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.theme.CryptoTheme

/**
 * @param baseSymbol Base asset symbol (e.g. "BTC")
 * @param quoteSymbol Quote asset symbol (e.g. "USDT"); empty hides the "/quote" label
 * @param imageUrl Coin image URL
 * @param price Formatted primary price string (e.g. "0.8934")
 * @param secondaryPrice Formatted secondary price string (e.g. "₩1,198.5"); null hides the line
 * @param volume Formatted trading volume string (e.g. "67.8M")
 * @param changePercent Change percentage value
 * @param onClick Callback when item is clicked
 * @param modifier Optional modifier
 */
@Composable
fun CoinListItem(
    baseSymbol: String,
    quoteSymbol: String,
    imageUrl: String,
    price: String,
    secondaryPrice: String?,
    volume: String,
    changePercent: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val badgeColor = when {
        changePercent > 0 -> CryptoTheme.colorScheme.changeBadge.backgroundUp
        changePercent < 0 -> CryptoTheme.colorScheme.changeBadge.backgroundDown
        else -> CryptoTheme.colorScheme.changeBadge.backgroundFlat
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
        // Left section: Avatar + Symbol/Volume
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
                    baseSymbol,
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CryptoTheme.colorScheme.icon.placeholderBackground),
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Base/Quote symbol and volume
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = baseSymbol,
                        style = CryptoTheme.typography.titleMedium,
                        color = CryptoTheme.colorScheme.text.primary,
                    )
                    if (quoteSymbol.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "/$quoteSymbol",
                            style = CryptoTheme.typography.bodyMedium,
                            color = CryptoTheme.colorScheme.text.secondary,
                        )
                    }
                }
                Text(
                    text = volume,
                    style = CryptoTheme.typography.bodySmall,
                    color = CryptoTheme.colorScheme.text.tertiary,
                )
            }
        }

        // Right section: Price/Secondary + Change badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = price,
                    style = CryptoTheme.typography.titleMedium,
                    color = CryptoTheme.colorScheme.text.primary,
                )
                if (secondaryPrice != null) {
                    Text(
                        text = secondaryPrice,
                        style = CryptoTheme.typography.bodySmall,
                        color = CryptoTheme.colorScheme.text.secondary,
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .widthIn(min = 76.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor)
                    .padding(horizontal = 11.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = changeText,
                    style = CryptoTheme.typography.labelLarge,
                    color = CryptoTheme.colorScheme.changeBadge.foreground,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CoinListItemUpPreview() {
    CoinListItem(
        baseSymbol = "MATIC",
        quoteSymbol = "USDT",
        imageUrl = "",
        price = "0.8934",
        secondaryPrice = "₩1,198.5",
        volume = "67.8M",
        changePercent = 1.75,
        onClick = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CoinListItemDownPreview() {
    CoinListItem(
        baseSymbol = "MATIC",
        quoteSymbol = "USDT",
        imageUrl = "",
        price = "0.8934",
        secondaryPrice = "₩1,198.5",
        volume = "67.8M",
        changePercent = -1.75,
        onClick = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CoinListItemFlatPreview() {
    CoinListItem(
        baseSymbol = "MATIC",
        quoteSymbol = "USDT",
        imageUrl = "",
        price = "0.8934",
        secondaryPrice = "₩1,198.5",
        volume = "67.8M",
        changePercent = 0.0,
        onClick = {},
    )
}
