package io.soma.cryptobook.core.designsystem.component.radio.color

import androidx.compose.material3.RadioButtonColors
import androidx.compose.runtime.Composable
import io.soma.cryptobook.core.designsystem.theme.theme.CryptoTheme

@Composable
fun cryptoRadioButtonColors(): RadioButtonColors = RadioButtonColors(
    selectedColor = CryptoTheme.colorScheme.filledButton.background,
    unselectedColor = CryptoTheme.colorScheme.icon.primary,
    disabledSelectedColor = CryptoTheme.colorScheme.filledButton.foregroundDisabled,
    disabledUnselectedColor = CryptoTheme.colorScheme.filledButton.foregroundDisabled,
)
