package io.soma.cryptobook.main.presentation.navigation

import androidx.core.net.toUri
import androidx.navigation3.runtime.NavKey
import io.soma.cryptobook.coindetail.presentation.navigation.CoinDetailNavKey
import io.soma.cryptobook.core.domain.navigation.AppPage
import io.soma.cryptobook.home.presentation.navigation.HomeNavKey
import io.soma.cryptobook.search.presentation.navigation.SearchNavKey
import io.soma.cryptobook.settings.presentation.navigation.SettingsNavKey
import javax.inject.Inject

class LinkRouter @Inject constructor() {
    fun resolve(link: String): NavKey {
        val uri = runCatching { link.toUri() }
            .getOrElse { return HomeNavKey }
        val segments = uri.pathSegments ?: emptyList()
        if (segments.isEmpty()) return HomeNavKey
        return when (segments[0]) {
            "coindetail" -> {
                val coinName = segments.getOrNull(1)
                    ?.takeIf { it.isNotBlank() }
                    ?: return HomeNavKey
                CoinDetailNavKey(coinName)
            }

            "home" -> HomeNavKey

            "settings" -> SettingsNavKey

            else -> HomeNavKey
        }
    }

    fun resolve(page: AppPage): NavKey = when (page) {
        is AppPage.Home -> HomeNavKey
        is AppPage.CoinDetail -> CoinDetailNavKey(page.coinName)
        is AppPage.Settings -> SettingsNavKey
        is AppPage.Search -> SearchNavKey
    }
}
