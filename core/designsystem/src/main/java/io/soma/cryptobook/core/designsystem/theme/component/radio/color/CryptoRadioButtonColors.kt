package io.soma.cryptobook.core.designsystem.theme.component.radio.color

import androidx.compose.material3.RadioButtonColors
import androidx.compose.runtime.Composable
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme

@Composable
fun cryptoRadioButtonColors(): RadioButtonColors = RadioButtonColors(
    selectedColor = CbTheme.colorScheme.filledButton.background,
    unselectedColor = CbTheme.colorScheme.icon.primary,
    disabledSelectedColor = CbTheme.colorScheme.filledButton.foregroundDisabled,
    disabledUnselectedColor = CbTheme.colorScheme.filledButton.foregroundDisabled,
)
