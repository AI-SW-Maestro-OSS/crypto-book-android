package io.soma.cryptobook.core.designsystem.theme.theme.type

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle

@Immutable
data class CryptoTypography(
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,
    val headlineSmall: TextStyle,
    val labelLarge: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
)