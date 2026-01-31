package io.soma.cryptobook.coindetail.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/**
 * Price change type for color variation
 */
enum class PriceChangeType {
    Up,
    Down,
    Flat,
}

/**
 * Price change component showing coin avatar, price, and change percentage
 *
 * Figma element name: PriceChange
 * Figma element type: Component
 * Figma node-id: 199:1818
 *
 * Displays:
 * - Coin avatar image (56x56dp, rounded)
 * - Price text (36sp Bold)
 * - Price change with percentage (18sp Bold, color by state)
 *
 * Dependencies: None (leaf component)
 *
 * Layout:
 * - Row with gap 12dp, fillMaxWidth
 * - Avatar: 56x56dp, rounded 28dp
 * - Price info: Column, weight 1f (fill remaining), gap 4dp, start aligned
 *
 * Variants:
 * - Up: Green (#22C55E)
 * - Down: Red (#E11919)
 * - Flat: White (#FAFAFA)
 *
 * @param price Formatted price string (e.g., "$73,500.89")
 * @param priceChangeText Formatted change text (e.g., "+1,840.55 (+2.58%)")
 * @param priceChangeType Price change direction (Up, Down, Flat)
 * @param modifier Optional modifier
 */
@Composable
fun PriceChange(
    imageUrl: String,
    price: String,
    priceChangeText: String,
    priceChangeType: PriceChangeType,
    modifier: Modifier = Modifier,
) {
    val changeColor = when (priceChangeType) {
        PriceChangeType.Up -> PriceUp
        PriceChangeType.Down -> PriceDown
        PriceChangeType.Flat -> PriceFlat
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ScreenBackground),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar placeholder
        AsyncImage(
            model = imageUrl,
            placeholder = painterResource(R.drawable.ic_coin_placeholder),
            error = painterResource(R.drawable.ic_coin_placeholder),
            fallback = painterResource(R.drawable.ic_coin_placeholder),
            contentDescription = "coin icon",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
        )
        // Price Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            // Price
            Text(
                text = price,
                modifier = Modifier.fillMaxWidth(),
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                lineHeight = 40.sp,
                color = textPrimary,
                textAlign = TextAlign.Start,
            )

            // Price Change
            Text(
                text = priceChangeText,
                modifier = Modifier.fillMaxWidth(),
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 28.sp,
                color = changeColor,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun PriceChangeUpPreview() {
    PriceChange(
        imageUrl = "",
        price = "$73,500.89",
        priceChangeText = "+1,840.55 (+2.58%)",
        priceChangeType = PriceChangeType.Up,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun PriceChangeDownPreview() {
    PriceChange(
        imageUrl = "",
        price = "$73,500.89",
        priceChangeText = "-1,840.55 (-2.58%)",
        priceChangeType = PriceChangeType.Down,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun PriceChangeFlatPreview() {
    PriceChange(
        imageUrl = "",
        price = "$73,500.89",
        priceChangeText = "+0.00 (0.00%)",
        priceChangeType = PriceChangeType.Flat,
    )
}
