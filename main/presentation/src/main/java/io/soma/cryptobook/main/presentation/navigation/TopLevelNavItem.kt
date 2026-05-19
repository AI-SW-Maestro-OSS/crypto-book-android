package io.soma.cryptobook.main.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.home.presentation.navigation.HomeNavKey
import io.soma.cryptobook.settings.presentation.navigation.SettingsNavKey

data class TopLevelNavItem(
    val icon: ImageVector,
    @StringRes val iconTextId: Int,
)

val HOME = TopLevelNavItem(
    icon = Icons.Outlined.Home,
    iconTextId = CryptoString.cb_top_level_home,
)

val SETTINGS = TopLevelNavItem(
    icon = Icons.Outlined.Settings,
    iconTextId = CryptoString.cb_top_level_settings,
)

val TOP_LEVEL_NAV_ITEMS = mapOf(
    HomeNavKey to HOME,
    SettingsNavKey to SETTINGS,
)
