package io.soma.cryptobook.main.presentation.navigation

import androidx.navigation3.runtime.NavKey
import io.soma.cryptobook.core.designsystem.component.navigation.model.NavigationItem
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.resource.CryptoDrawable
import io.soma.cryptobook.diary.presentation.navigation.DiaryNavKey
import io.soma.cryptobook.home.presentation.navigation.HomeNavKey
import io.soma.cryptobook.settings.presentation.navigation.SettingsNavKey
import io.soma.cryptobook.watchlist.presentation.navigation.WatchlistNavKey

enum class CryptoTopLevelNavItem(
    override val iconRes: Int,
    override val iconResSelected: Int,
    override val labelRes: Int,
    val navKey: NavKey,
) : NavigationItem {
    HOME(
        iconRes = CryptoDrawable.ic_home,
        iconResSelected = CryptoDrawable.ic_home_filled,
        labelRes = CryptoString.crypto_top_level_home,
        navKey = HomeNavKey,
    ),
    WATCHLIST(
        iconRes = CryptoDrawable.ic_watchlists,
        iconResSelected = CryptoDrawable.ic_watchlists_filled,
        labelRes = CryptoString.crypto_top_level_watchlist,
        navKey = WatchlistNavKey,
    ),
    DIARY(
        iconRes = CryptoDrawable.ic_diary,
        iconResSelected = CryptoDrawable.ic_diary_filled,
        labelRes = CryptoString.crypto_top_level_diary,
        navKey = DiaryNavKey,
    ),
    SETTINGS(
        iconRes = CryptoDrawable.ic_settings,
        iconResSelected = CryptoDrawable.ic_settings_filled,
        labelRes = CryptoString.crypto_top_level_settings,
        navKey = SettingsNavKey,
    ),
}
