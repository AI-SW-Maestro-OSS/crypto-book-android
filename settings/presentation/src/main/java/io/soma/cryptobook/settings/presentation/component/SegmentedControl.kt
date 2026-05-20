package io.soma.cryptobook.settings.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.fontFamily
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme

/**
 * Segmented control component for selecting between two options
 *
 * Figma element name: ControlSlot
 * Figma element type: Frame
 * Figma node-id: 177:706
 *
 * A two-option segmented button control used in settings cards.
 *
 * Dependencies: None (leaf component)
 *
 * @param options List of two option labels to display
 * @param selectedIndex Index of the currently selected option (0 or 1)
 * @param onOptionSelected Callback when an option is selected
 * @param modifier Optional modifier for the component
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(
                color = CbTheme.colorScheme.toggleButton.backgroundOff,
                shape = RoundedCornerShape(6.dp),
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        color = if (isSelected) {
                            CbTheme.colorScheme.toggleButton.backgroundOn
                        } else {
                            CbTheme.colorScheme.toggleButton.backgroundOff
                        },
                    )
                    .clickable { onOptionSelected(index) }
                    .padding(horizontal = 13.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = if (isSelected) {
                        CbTheme.colorScheme.toggleButton.foregroundOn
                    } else {
                        CbTheme.colorScheme.toggleButton.foregroundOff
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SegmentedControlPreview() {
    SegmentedControl(
        options = listOf(
            stringResource(CryptoString.cb_settings_currency_dollar),
            stringResource(CryptoString.cb_settings_currency_won),
        ),
        selectedIndex = 0,
        onOptionSelected = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SegmentedControlLanguagePreview() {
    SegmentedControl(
        options = listOf(
            stringResource(CryptoString.cb_settings_language_english),
            stringResource(CryptoString.cb_settings_language_korean),
        ),
        selectedIndex = 1,
        onOptionSelected = {},
    )
}
