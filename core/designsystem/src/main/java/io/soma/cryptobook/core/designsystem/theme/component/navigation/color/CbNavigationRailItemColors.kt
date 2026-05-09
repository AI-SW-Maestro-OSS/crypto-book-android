package io.soma.cryptobook.core.designsystem.theme.component.navigation.color

import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme

@Composable
fun cbNavigationRailItemColors(): NavigationRailItemColors = NavigationRailItemColors(
    selectedIconColor = CbTheme.colorScheme.icon.secondary,
    unselectedIconColor = CbTheme.colorScheme.icon.primary,
    disabledIconColor = CbTheme.colorScheme.outlineButton.foregroundDisabled,
    selectedTextColor = CbTheme.colorScheme.icon.secondary,
    unselectedTextColor = CbTheme.colorScheme.icon.primary,
    disabledTextColor = CbTheme.colorScheme.outlineButton.foregroundDisabled,
    selectedIndicatorColor = Color.Transparent,
)