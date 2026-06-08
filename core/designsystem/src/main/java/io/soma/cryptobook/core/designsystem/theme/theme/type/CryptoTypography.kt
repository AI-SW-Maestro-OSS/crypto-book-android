package io.soma.cryptobook.core.designsystem.theme.theme.type

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle

@Immutable
data class CryptoTypography(
    val labelMedium: TextStyle,
    val titleMedium: TextStyle,
    val labelSmall: TextStyle,
    val headlineSmall: TextStyle,
    val headlineMedium: TextStyle,
    val labelLarge: TextStyle,
    val labelLargeRegular: TextStyle,
    val bodySmall: TextStyle,
    val displaySmall: TextStyle,
    val titleLarge: TextStyle,
    val bodyLargeEmphasis: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
)