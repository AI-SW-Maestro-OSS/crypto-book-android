package io.soma.cryptobook.main.presentation.navigation

import androidx.navigation3.runtime.NavKey
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.component.navigation.model.NavigationItem
import io.soma.cryptobook.core.designsystem.theme.resource.CbDrawable
import io.soma.cryptobook.diary.presentation.navigation.DiaryNavKey
import io.soma.cryptobook.home.presentation.navigation.HomeNavKey
import io.soma.cryptobook.settings.presentation.navigation.SettingsNavKey
import io.soma.cryptobook.watchlist.presentation.navigation.WatchlistNavKey

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
    WATCHLIST(
        iconRes = CbDrawable.ic_watchlists,
        iconResSelected = CbDrawable.ic_watchlists_filled,
        labelRes = CryptoString.cb_top_level_watchlist,
        navKey = WatchlistNavKey,
    ),
    DIARY(
        iconRes = CbDrawable.ic_diary,
        iconResSelected = CbDrawable.ic_diary_filled,
        labelRes = CryptoString.cb_top_level_diary,
        navKey = DiaryNavKey,
    ),
    SETTINGS(
        iconRes = CbDrawable.ic_settings,
        iconResSelected = CbDrawable.ic_settings_filled,
        labelRes = CryptoString.cb_top_level_settings,
        navKey = SettingsNavKey,
    ),
}
