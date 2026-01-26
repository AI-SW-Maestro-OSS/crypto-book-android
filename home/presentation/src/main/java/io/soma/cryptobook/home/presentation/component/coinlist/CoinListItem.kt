package io.soma.cryptobook.home.presentation.component.coinlist

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.soma.cryptobook.core.designsystem.R
import io.soma.cryptobook.core.designsystem.theme.PriceDown
import io.soma.cryptobook.core.designsystem.theme.PriceFlat
import io.soma.cryptobook.core.designsystem.theme.PriceUp
import io.soma.cryptobook.core.designsystem.theme.ScreenBackground
import io.soma.cryptobook.core.designsystem.theme.fontFamily
import io.soma.cryptobook.core.designsystem.theme.textPrimary
import io.soma.cryptobook.core.designsystem.theme.textSecondary

/**
 * Coin list item component
 *
 * Figma element name: Table (PriceRow variant)
 * Figma element type: Instance
 * Figma node-id: 177:529
 *
 * Displays:
 * - Avatar: 40x40dp rounded placeholder (TODO: 실제 코인 이미지)
 * - Symbol: BTCUSDT (16px, SemiBold)
 * - Name: Bitcoin (12px, Regular, secondary color)
 * - Price: $68500.52 (16px, Bold, right-aligned)
 * - Change %: +1.75% (12px, Bold, color by state)
 *
 * Dependencies: None (leaf component)
 *
 * Layout:
 * - Width: fillMaxWidth
 * - Padding: horizontal 15dp, vertical 9dp
 * - Left: Avatar + Text info (11dp gap between avatar and text)
 * - Right: Price + Change % (end-aligned)
 *
 * Price change colors:
 * - Up: #22C55E (green)
 * - Down: #E11919 (red)
 * - Flat: #FAFAFA (white)
 *
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
        changePercent > 0 -> PriceUp
        changePercent < 0 -> PriceDown
        else -> PriceFlat
    }

    val changeText = when {
        changePercent >= 0 -> "+${String.format("%.2f", changePercent)}%"
        else -> "${String.format("%.2f", changePercent)}%"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ScreenBackground)
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
                contentDescription = "$name coin icon",
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
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = textPrimary,
                )
                Text(
                    text = name,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = textSecondary,
                )
            }
        }

        // Right section: Price + Change
        Column(
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = price,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = textPrimary,
                textAlign = TextAlign.End,
            )
            Text(
                text = changeText,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = changeColor,
                textAlign = TextAlign.End,
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
