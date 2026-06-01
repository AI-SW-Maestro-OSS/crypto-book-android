package io.soma.cryptobook.core.designsystem.theme.component.button.color

import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme

@Composable
fun cryptoTextButtonColors(
    contentColor: Color = CbTheme.colorScheme.outlineButton.foreground,
): ButtonColors = ButtonColors(
    containerColor = Color.Transparent,
    contentColor = contentColor,
    disabledContainerColor = Color.Transparent,
    disabledContentColor = CbTheme.colorScheme.outlineButton.foregroundDisabled,
)
