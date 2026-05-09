package io.soma.cryptobook.core.designsystem.theme.theme.color

import androidx.compose.ui.graphics.Color

val darkCbColorScheme: CbColorScheme = CbColorScheme(
    text = CbColorScheme.TextColors(
        primary = Color(0xFFF3F4F6),
        secondary = Color(0xFFBDC1CA),
        interaction = Color(0xFF0d59f2)
    ),
    background = CbColorScheme.BackgroundColors(
        primary = Color(0xFF1A1A1A),
        secondary = Color(0xFF1A1A1A)
    ),
    icon = CbColorScheme.IconColors(
        primary = Color(0xFFF3F4F6),
        secondary = Color(0xFF0d59f2)
    ),
    filledButton = CbColorScheme.FilledButtonColors(
        foregroundDisabled = Color(color = 0xFF657185)
    ),
    stroke = CbColorScheme.StrokeColors(
        border = Color(0xFF0d59f2)
    ),
    outlineButton = CbColorScheme.OutlineButtonColors(
        foregroundDisabled = Color(0xFFf3f4f6)
    )
)