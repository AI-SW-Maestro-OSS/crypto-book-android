package io.soma.cryptobook.core.designsystem.theme.theme.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class CbColorScheme(
    val text: TextColors,
    val background: BackgroundColors,
    val icon: IconColors,
    val filledButton: FilledButtonColors,
    val outlineButton: OutlineButtonColors,
    val stroke: StrokeColors,
    val toggleButton: ToggleButtonColors,
    val price: PriceColors,
) {
    @Immutable
    data class TextColors(
        val primary: Color,
        val secondary: Color,
        val interaction: Color,
    )

    @Immutable
    data class BackgroundColors(
        val primary: Color,
        val secondary: Color,
        val tertiary: Color,
        val selected: Color,
    )

    @Immutable
    data class StrokeColors(
        val border: Color,
    )

    @Immutable
    data class IconColors(
        val primary: Color,
        val secondary: Color,
    )

    @Immutable
    data class FilledButtonColors(
        val foregroundDisabled: Color,
    )

    @Immutable
    data class OutlineButtonColors(
        val foregroundDisabled: Color,
    )

    @Immutable
    data class ToggleButtonColors(
        val backgroundOn: Color,
        val backgroundOff: Color,
        val foregroundOn: Color,
        val foregroundOff: Color,
    )

    @Immutable
    data class PriceColors(
        val up: Color,
        val down: Color,
        val flat: Color,
    )
}