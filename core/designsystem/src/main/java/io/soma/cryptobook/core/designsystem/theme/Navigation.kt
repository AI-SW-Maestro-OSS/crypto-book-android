package io.soma.cryptobook.core.designsystem.theme

import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.runtime.Composable
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme


@Composable
fun cbNavigationItemColors() = NavigationSuiteItemColors(
    navigationBarItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = CbNavigationDefaults.navigationSelectedItemColor(),
        unselectedIconColor = CbNavigationDefaults.navigationDefaultItemColor(),
        selectedTextColor = CbNavigationDefaults.navigationSelectedItemColor(),
        unselectedTextColor = CbNavigationDefaults.navigationDefaultItemColor(),
        indicatorColor = CbNavigationDefaults.navigationIndicatorColor(),
    ),
    navigationRailItemColors = NavigationRailItemDefaults.colors(
        selectedIconColor = CbNavigationDefaults.navigationSelectedItemColor(),
        unselectedIconColor = CbNavigationDefaults.navigationDefaultItemColor(),
        selectedTextColor = CbNavigationDefaults.navigationSelectedItemColor(),
        unselectedTextColor = CbNavigationDefaults.navigationDefaultItemColor(),
        indicatorColor = CbNavigationDefaults.navigationIndicatorColor(),
    ),
    navigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
        selectedIconColor = CbNavigationDefaults.navigationSelectedItemColor(),
        unselectedIconColor = CbNavigationDefaults.navigationDefaultItemColor(),
        selectedTextColor = CbNavigationDefaults.navigationSelectedItemColor(),
        unselectedTextColor = CbNavigationDefaults.navigationDefaultItemColor(),
    ),
)

object CbNavigationDefaults {
    @Composable
    fun navigationDefaultItemColor() = CbTheme.colorScheme.icon.primary

    @Composable
    fun navigationSelectedItemColor() = CbTheme.colorScheme.icon.secondary

    @Composable
    fun navigationIndicatorColor() = CbTheme.colorScheme.background.selected
}
