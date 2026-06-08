package io.soma.cryptobook.core.designsystem.component.field.color

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.soma.cryptobook.core.designsystem.theme.theme.CryptoTheme

@Composable
fun cryptoTextFieldColors(
    textColor: Color = CryptoTheme.colorScheme.text.primary,
): TextFieldColors = TextFieldDefaults.colors(
    focusedTextColor = textColor,
    unfocusedTextColor = textColor,

    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,

    cursorColor = CryptoTheme.colorScheme.text.interaction,
    selectionColors = TextSelectionColors(
        handleColor = CryptoTheme.colorScheme.stroke.border,
        backgroundColor = CryptoTheme.colorScheme.stroke.border.copy(alpha = 0.4f)
    ),

    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,

    focusedTrailingIconColor = CryptoTheme.colorScheme.icon.primary,
    unfocusedTrailingIconColor = CryptoTheme.colorScheme.icon.primary,

    focusedPlaceholderColor = CryptoTheme.colorScheme.text.secondary,
    unfocusedPlaceholderColor = CryptoTheme.colorScheme.text.secondary,
)