package io.soma.cryptobook.watchlist.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.soma.cryptobook.watchlist.presentation.WatchlistScreen

fun EntryProviderScope<NavKey>.watchlistEntry(onNavigateToCoinDetail: (String) -> Unit) {
    entry<WatchlistNavKey> {
        WatchlistScreen(onNavigateToCoinDetail = onNavigateToCoinDetail)
    }
}
