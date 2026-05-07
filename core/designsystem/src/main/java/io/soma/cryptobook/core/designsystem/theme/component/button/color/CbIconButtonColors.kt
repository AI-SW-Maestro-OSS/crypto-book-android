package io.soma.cryptobook.core.designsystem.theme.component.button.color

import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme

@Composable
fun cbStandardIconButtonColors(
    contentColor: Color = CbTheme.colorScheme.icon.primary,
): IconButtonColors = IconButtonColors(
    containerColor = Color.Transparent,
    contentColor = contentColor,
    disabledContainerColor = Color.Transparent,
    disabledContentColor = CbTheme.colorScheme.filledButton.foregroundDisabled,
)