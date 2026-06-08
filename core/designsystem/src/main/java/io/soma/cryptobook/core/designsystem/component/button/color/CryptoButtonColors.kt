package io.soma.cryptobook.core.designsystem.component.button.color

import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.soma.cryptobook.core.designsystem.theme.theme.CryptoTheme

@Composable
fun cryptoTextButtonColors(
    contentColor: Color = CryptoTheme.colorScheme.outlineButton.foreground,
): ButtonColors = ButtonColors(
    containerColor = Color.Transparent,
    contentColor = contentColor,
    disabledContainerColor = Color.Transparent,
    disabledContentColor = CryptoTheme.colorScheme.outlineButton.foregroundDisabled,
)
