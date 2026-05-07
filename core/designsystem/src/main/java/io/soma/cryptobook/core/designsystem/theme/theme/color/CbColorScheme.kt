package io.soma.cryptobook.core.designsystem.theme.theme.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class CbColorScheme(
    val text: TextColors,
    val background: BackgroundColors,
    val icon: IconColors,
    val filledButton: FilledButtonColors,
) {
    @Immutable
    data class TextColors(
        val primary: Color,
    )

    @Immutable
    data class BackgroundColors(
        val secondary: Color,
    )

    @Immutable
    data class IconColors(
        val primary: Color,
    )

    @Immutable
    data class FilledButtonColors(
        val foregroundDisabled: Color,
    )
}