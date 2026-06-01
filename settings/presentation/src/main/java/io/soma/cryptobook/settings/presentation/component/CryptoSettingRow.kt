package io.soma.cryptobook.settings.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.soma.cryptobook.core.designsystem.R
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme

/**
 * Tappable settings row: a bold title on the left, the current value and a chevron
 * on the right. Tapping the row typically opens an option dialog.
 *
 * Figma node-id: 376:1557 (SettingRow)
 *
 * @param title Setting name (Bold 16sp).
 * @param value Current value shown on the right (Medium 14sp).
 * @param onClick Invoked when the row is tapped.
 * @param modifier Optional modifier for the row.
 */
@Composable
fun CryptoSettingRow(
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 60.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = CbTheme.typography.bodyLarge,
            color = CbTheme.colorScheme.text.primary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = CbTheme.typography.bodyMedium,
            color = CbTheme.colorScheme.text.secondary,
        )
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = CbTheme.colorScheme.text.secondary,
        )
    }
}
