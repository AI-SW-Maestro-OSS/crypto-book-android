package io.soma.cryptobook.core.designsystem.theme.theme.color

import androidx.compose.ui.graphics.Color

val darkCryptoColorScheme: CryptoColorScheme = CryptoColorScheme(
    text = CryptoColorScheme.TextColors(
        primary = Color(0xFFF3F4F6),
        secondary = Color(0xFFBDC1CA),
        tertiary = Color(0xFF6B7280),
        interaction = Color(0xFF0d59f2)
    ),
    background = CryptoColorScheme.BackgroundColors(
        primary = Color(0xFF1A1A1A),
        secondary = Color(0xFF1A1A1A),
        tertiary = Color(0xFF1E2128),
        selected = Color(0xFF262A33)
    ),
    icon = CryptoColorScheme.IconColors(
        primary = Color(0xFFF3F4F6),
        secondary = Color(0xFF0d59f2),
        placeholderBackground = Color(0xFF2A2A2A)
    ),
    filledButton = CryptoColorScheme.FilledButtonColors(
        background = Color(0xFF0D59F2),
        foreground = Color(0xFFFFFFFF),
        foregroundDisabled = Color(color = 0xFF657185)
    ),
    stroke = CryptoColorScheme.StrokeColors(
        border = Color(0xFF0d59f2),
        divider = Color(0xFF383838)
    ),
    outlineButton = CryptoColorScheme.OutlineButtonColors(
        foreground = Color(0xFF0D59F2),
        foregroundDisabled = Color(0xFFf3f4f6)
    ),
    toggleButton = CryptoColorScheme.ToggleButtonColors(
        backgroundOn = Color(0xFF0D59F2),
        backgroundOff = Color(0xFF262A33),
        foregroundOn = Color(0xFFFFFFFF),
        foregroundOff = Color(0xFFF3F4F6)
    ),
    price = CryptoColorScheme.PriceColors(
        up = Color(0xFF22C55E),
        down = Color(0xFFE11919),
        flat = Color(0xFFFAFAFA)
    ),
    changeBadge = CryptoColorScheme.ChangeBadgeColors(
        backgroundUp = Color(0xFF22C55E),
        backgroundDown = Color(0xFFDC2626),
        backgroundFlat = Color(0xFF4B5563),
        foreground = Color(0xFFFFFFFF)
    ),
    status = CryptoColorScheme.StatusColors(
        error = Color(color = 0xFFFF4E63)
    )
)

val lightCryptoColorScheme: CryptoColorScheme = CryptoColorScheme(
    text = CryptoColorScheme.TextColors(
        primary = Color(0xFF1A1A1A),
        secondary = Color(0xFF9CA3AF),
        tertiary = Color(0xFF6B7280),
        interaction = Color(0xFF0d59f2)
    ),
    background = CryptoColorScheme.BackgroundColors(
        primary = Color(0xFFFFFFFF),
        secondary = Color(0xFFFFFFFF),
        tertiary = Color(0xFFF3F4F6),
        selected = Color(0xFFE5E7EB)
    ),
    icon = CryptoColorScheme.IconColors(
        primary = Color(0xFF1A1A1A),
        secondary = Color(0xFF0d59f2),
        placeholderBackground = Color(0xFFF3F4F6)
    ),
    filledButton = CryptoColorScheme.FilledButtonColors(
        background = Color(0xFF0d59f2),
        foreground = Color(0xFFFFFFFF),
        foregroundDisabled = Color(color = 0xFF9CA3AF)
    ),
    stroke = CryptoColorScheme.StrokeColors(
        border = Color(0xFF0d59f2),
        divider = Color(0xFFE5E7EB)
    ),
    outlineButton = CryptoColorScheme.OutlineButtonColors(
        foreground = Color(0xFF0D59F2),
        foregroundDisabled = Color(0xFF1A1A1A)
    ),
    toggleButton = CryptoColorScheme.ToggleButtonColors(
        backgroundOn = Color(0xFF0D59F2),
        backgroundOff = Color(0xFFE5E7EB),
        foregroundOn = Color(0xFFFFFFFF),
        foregroundOff = Color(0xFF1A1A1A)
    ),
    price = CryptoColorScheme.PriceColors(
        up = Color(0xFF22C55E),
        down = Color(0xFFE11919),
        flat = Color(0xFFFAFAFA)
    ),
    changeBadge = CryptoColorScheme.ChangeBadgeColors(
        backgroundUp = Color(0xFF22C55E),
        backgroundDown = Color(0xFFDC2626),
        backgroundFlat = Color(0xFF4B5563),
        foreground = Color(0xFFFFFFFF)
    ),
    status = CryptoColorScheme.StatusColors(
        error = Color(color = 0xFFCB263A)
    )
)