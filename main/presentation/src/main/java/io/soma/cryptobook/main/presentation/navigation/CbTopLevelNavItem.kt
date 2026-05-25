package io.soma.cryptobook.main.presentation.navigation

import androidx.navigation3.runtime.NavKey
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.component.navigation.model.NavigationItem
import io.soma.cryptobook.core.designsystem.theme.resource.CbDrawable
import io.soma.cryptobook.home.presentation.navigation.HomeNavKey
import io.soma.cryptobook.settings.presentation.navigation.SettingsNavKey

enum class CbTopLevelNavItem(
    override val iconRes: Int,
    override val iconResSelected: Int,
    override val labelRes: Int,
    val navKey: NavKey,
) : NavigationItem {
    HOME(
        iconRes = CbDrawable.ic_home,
        iconResSelected = CbDrawable.ic_home_filled,
        labelRes = CryptoString.cb_top_level_home,
        navKey = HomeNavKey,
    ),
    SETTINGS(
        iconRes = CbDrawable.ic_settings,
        iconResSelected = CbDrawable.ic_settings_filled,
        labelRes = CryptoString.cb_top_level_settings,
        navKey = SettingsNavKey,
    ),
}
