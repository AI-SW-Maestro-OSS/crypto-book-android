package io.soma.cryptobook.settings.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.soma.cryptobook.core.designsystem.R
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.theme.CryptoTheme

/**
 * Exchange rate card component
 *
 * Figma element name: Container (Exchange Rate)
 * Figma element type: Frame
 * Figma node-id: 180:192
 *
 * Displays exchange rate information with a refresh button.
 *
 * Dependencies: None (leaf component)
 *
 * @param title Title text (e.g., "Exchange Rate")
 * @param rateText Exchange rate value (e.g., "1 USD = 1,450 WON")
 * @param updateTimeText Update time text (e.g., "Rates updated just now")
 * @param onRefreshClick Callback when refresh button is clicked
 * @param modifier Optional modifier for the component
 */
@Composable
fun ExchangeRateCard(
    title: String,
    rateText: String,
    updateTimeText: String,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = CryptoTheme.colorScheme.background.tertiary,
                shape = RoundedCornerShape(10.dp),
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = CryptoTheme.typography.titleMedium,
                color = CryptoTheme.colorScheme.text.primary,
            )
            Text(
                text = rateText,
                style = CryptoTheme.typography.headlineMedium,
                color = CryptoTheme.colorScheme.text.primary,
            )
            Text(
                text = updateTimeText,
                style = CryptoTheme.typography.bodySmall,
                color = CryptoTheme.colorScheme.text.secondary,
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_refresh),
            contentDescription = stringResource(
                CryptoString.crypto_settings_exchange_rate_refresh_cd,
            ),
            tint = CryptoTheme.colorScheme.text.primary,
            modifier = Modifier
                .size(32.dp)
                .clickable { onRefreshClick() },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ExchangeRateCardPreview() {
    ExchangeRateCard(
        title = stringResource(CryptoString.crypto_settings_exchange_rate_title),
        rateText = stringResource(CryptoString.crypto_settings_exchange_rate_format, "1,450"),
        updateTimeText = stringResource(CryptoString.crypto_settings_exchange_rate_updated_now),
        onRefreshClick = {},
    )
}
