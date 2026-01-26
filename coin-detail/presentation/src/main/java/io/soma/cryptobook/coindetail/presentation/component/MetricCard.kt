package io.soma.cryptobook.coindetail.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.soma.cryptobook.core.designsystem.theme.fontFamily
import io.soma.cryptobook.core.designsystem.theme.surfaceCardDefault
import io.soma.cryptobook.core.designsystem.theme.textPrimary
import io.soma.cryptobook.core.designsystem.theme.textSecondary

/**
 * Metric card grid container displaying 2×2 grid of metrics
 *
 * Figma element name: Frame
 * Figma element type: Frame
 * Figma node-id: 177:686
 *
 * Displays:
 * - Row 1: 24h High, 24h Low
 * - Row 2: 24h Volume, Open Price
 *
 * Dependencies:
 * - [MetricCard] - Individual metric card component
 *
 * Layout:
 * - Column with 8dp vertical gap
 * - 2 Rows with 8dp horizontal gap each
 * - Cards use weight(1f) to fill grid cells equally
 *
 * @param high24h 24-hour high price (formatted, e.g., "$73,800.00")
 * @param low24h 24-hour low price (formatted, e.g., "$68,500.00")
 * @param volume24h 24-hour volume (formatted, e.g., "100M USDT")
 * @param openPrice Opening price (formatted, e.g., "$69,000.00")
 * @param modifier Optional modifier for the container
 */
@Composable
fun MetricCardGridContainer(
    high24h: String,
    low24h: String,
    volume24h: String,
    openPrice: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Row 1: 24h High, 24h Low
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricCard(
                label = "24h High",
                value = high24h,
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                label = "24h Low",
                value = low24h,
                modifier = Modifier.weight(1f),
            )
        }

        // Row 2: 24h Volume, Open Price
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricCard(
                label = "24h Volume",
                value = volume24h,
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                label = "Open Price",
                value = openPrice,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Metric card component displaying a label and value
 *
 * Figma element name: Stats/MetricCard
 * Figma element type: Component
 * Figma node-id: 177:678
 *
 * Displays:
 * - Label text (14sp Regular, #BDC1CA)
 * - Value text (16sp Medium, #F3F4F6)
 *
 * Dependencies: None (leaf component)
 *
 * Layout:
 * - Column with 5dp gap
 * - Container: 175x72dp, background #1E2128, rounded 10dp
 * - Content padding: 16dp horizontal, 13dp vertical
 *
 * @param label Label text (e.g., "24h High", "24h Low", "Volume")
 * @param value Value text (e.g., "$73,800.00", "100M USDT")
 * @param modifier Optional modifier for the component
 */
@Composable
fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(175.dp)
            .height(72.dp)
            .background(
                color = surfaceCardDefault,
                shape = RoundedCornerShape(10.dp),
            )
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        // Label
        Text(
            text = label,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = textSecondary,
        )

        // Value
        Text(
            text = value,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = textPrimary,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun MetricCard24hHighPreview() {
    MetricCard(
        label = "24h High",
        value = "$73,800.00",
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun MetricCard24hLowPreview() {
    MetricCard(
        label = "24h Low",
        value = "$68,200.00",
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun MetricCardVolumePreview() {
    MetricCard(
        label = "Volume (24h)",
        value = "100M USDT",
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun MetricCardGridContainerPreview() {
    MetricCardGridContainer(
        high24h = "$73,800.00",
        low24h = "$68,500.00",
        volume24h = "$25.4B",
        openPrice = "$69,000.00",
    )
}
