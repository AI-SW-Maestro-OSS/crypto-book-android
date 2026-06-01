package io.soma.cryptobook.settings.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme

/**
 * Rounded grouped container for settings rows.
 *
 * Figma node-id: 376:1556 (Container). Place [CryptoSettingRow] /
 * [CryptoSettingSelectionRow] children inside, separated by [CryptoSettingDivider].
 *
 * @param modifier Optional modifier for the card.
 * @param content Rows to render inside the card.
 */
@Composable
fun CryptoSettingCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CbTheme.colorScheme.background.tertiary),
        content = content,
    )
}

/**
 * Thin divider used between rows inside a [CryptoSettingCard].
 */
@Composable
fun CryptoSettingDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = CbTheme.colorScheme.stroke.divider,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun CryptoSettingCardPreview() {
    CryptoSettingCard {
        CryptoSettingRow(title = "Theme", value = "System default", onClick = {})
        CryptoSettingDivider()
        CryptoSettingRow(title = "Language", value = "한국어", onClick = {})
    }
}
