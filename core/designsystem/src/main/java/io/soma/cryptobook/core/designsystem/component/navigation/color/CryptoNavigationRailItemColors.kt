package io.soma.cryptobook.core.designsystem.component.navigation.color

import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.soma.cryptobook.core.designsystem.theme.theme.CryptoTheme

@Composable
fun cryptoNavigationRailItemColors(): NavigationRailItemColors = NavigationRailItemColors(
    selectedIconColor = CryptoTheme.colorScheme.icon.secondary,
    unselectedIconColor = CryptoTheme.colorScheme.icon.primary,
    disabledIconColor = CryptoTheme.colorScheme.outlineButton.foregroundDisabled,
    selectedTextColor = CryptoTheme.colorScheme.icon.secondary,
    unselectedTextColor = CryptoTheme.colorScheme.icon.primary,
    disabledTextColor = CryptoTheme.colorScheme.outlineButton.foregroundDisabled,
    selectedIndicatorColor = Color.Transparent,
)