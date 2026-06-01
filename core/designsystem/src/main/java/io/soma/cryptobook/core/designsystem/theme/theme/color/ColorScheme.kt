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
        secondary = Color(0xFF1A1A1A),
        tertiary = Color(0xFF1E2128),
        selected = Color(0xFF262A33)
    ),
    icon = CbColorScheme.IconColors(
        primary = Color(0xFFF3F4F6),
        secondary = Color(0xFF0d59f2)
    ),
    filledButton = CbColorScheme.FilledButtonColors(
        background = Color(0xFF0D59F2),
        foregroundDisabled = Color(color = 0xFF657185)
    ),
    stroke = CbColorScheme.StrokeColors(
        border = Color(0xFF0d59f2),
        divider = Color(0xFF383838)
    ),
    outlineButton = CbColorScheme.OutlineButtonColors(
        foreground = Color(0xFF0D59F2),
        foregroundDisabled = Color(0xFFf3f4f6)
    ),
    toggleButton = CbColorScheme.ToggleButtonColors(
        backgroundOn = Color(0xFF0D59F2),
        backgroundOff = Color(0xFF262A33),
        foregroundOn = Color(0xFFFFFFFF),
        foregroundOff = Color(0xFFF3F4F6)
    ),
    price = CbColorScheme.PriceColors(
        up = Color(0xFF22C55E),
        down = Color(0xFFE11919),
        flat = Color(0xFFFAFAFA)
    )
)

val lightCbColorScheme: CbColorScheme = CbColorScheme(
    text = CbColorScheme.TextColors(
        primary = Color(0xFF1A1A1A),
        secondary = Color(0xFF9CA3AF),
        interaction = Color(0xFF0d59f2)
    ),
    background = CbColorScheme.BackgroundColors(
        primary = Color(0xFFFFFFFF),
        secondary = Color(0xFFFFFFFF),
        tertiary = Color(0xFFF3F4F6),
        selected = Color(0xFFE5E7EB)
    ),
    icon = CbColorScheme.IconColors(
        primary = Color(0xFF1A1A1A),
        secondary = Color(0xFF0d59f2)
    ),
    filledButton = CbColorScheme.FilledButtonColors(
        background = Color(0xFF0d59f2),
        foregroundDisabled = Color(color = 0xFF9CA3AF)
    ),
    stroke = CbColorScheme.StrokeColors(
        border = Color(0xFF0d59f2),
        divider = Color(0xFFE5E7EB)
    ),
    outlineButton = CbColorScheme.OutlineButtonColors(
        foreground = Color(0xFF0D59F2),
        foregroundDisabled = Color(0xFF1A1A1A)
    ),
    toggleButton = CbColorScheme.ToggleButtonColors(
        backgroundOn = Color(0xFF0D59F2),
        backgroundOff = Color(0xFFE5E7EB),
        foregroundOn = Color(0xFFFFFFFF),
        foregroundOff = Color(0xFF1A1A1A)
    ),
    price = CbColorScheme.PriceColors(
        up = Color(0xFF22C55E),
        down = Color(0xFFE11919),
        flat = Color(0xFFFAFAFA)
    )
)