package io.soma.cryptobook.core.designsystem.theme.component.field.color

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme

@Composable
fun cbTextFieldColors(
    textColor: Color = CbTheme.colorScheme.text.primary,
): TextFieldColors = TextFieldDefaults.colors(
    focusedTextColor = textColor,
    unfocusedTextColor = textColor,

    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,

    cursorColor = CbTheme.colorScheme.text.interaction,
    selectionColors = TextSelectionColors(
        handleColor = CbTheme.colorScheme.stroke.border,
        backgroundColor = CbTheme.colorScheme.stroke.border.copy(alpha = 0.4f)
    ),

    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,

    focusedTrailingIconColor = CbTheme.colorScheme.icon.primary,
    unfocusedTrailingIconColor = CbTheme.colorScheme.icon.primary,

    focusedPlaceholderColor = CbTheme.colorScheme.text.secondary,
    unfocusedPlaceholderColor = CbTheme.colorScheme.text.secondary,
)