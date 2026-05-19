package io.soma.cryptobook.settings.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.fontFamily
import io.soma.cryptobook.core.designsystem.theme.surfaceCardDefault
import io.soma.cryptobook.core.designsystem.theme.textPrimary
import io.soma.cryptobook.core.designsystem.theme.textSecondary

/**
 * Settings option card component
 *
 * Figma element name: Settings (Property 1=OptionCard)
 * Figma element type: Instance
 * Figma node-id: 177:713, 177:820
 *
 * Displays a settings option with title, description, and a segmented control.
 *
 * Dependencies:
 * - [SegmentedControl]
 *
 * @param title Title text of the option (Bold 16px)
 * @param description Description text below the title (Regular 14px)
 * @param options List of option labels for the segmented control
 * @param selectedIndex Currently selected option index
 * @param onOptionSelected Callback when an option is selected
 * @param modifier Optional modifier for the component
 */
@Composable
fun SettingsOptionCard(
    title: String,
    description: String,
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = surfaceCardDefault,
                shape = RoundedCornerShape(10.dp),
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = textPrimary,
            )
            Text(
                text = description,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = textSecondary,
            )
        }
        SegmentedControl(
            options = options,
            selectedIndex = selectedIndex,
            onOptionSelected = onOptionSelected,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SettingsOptionCardCurrencyPreview() {
    SettingsOptionCard(
        title = stringResource(CryptoString.cb_settings_currency_unit_title),
        description = stringResource(CryptoString.cb_settings_currency_unit_description),
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
private fun SettingsOptionCardLanguagePreview() {
    SettingsOptionCard(
        title = stringResource(CryptoString.cb_settings_language_title),
        description = stringResource(CryptoString.cb_settings_language_description),
        options = listOf(
            stringResource(CryptoString.cb_settings_language_english),
            stringResource(CryptoString.cb_settings_language_korean),
        ),
        selectedIndex = 1,
        onOptionSelected = {},
    )
}
