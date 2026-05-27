package io.soma.cryptobook.watchlist.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.soma.cryptobook.watchlist.presentation.WatchlistRoute

fun EntryProviderScope<NavKey>.watchlistEntry() {
    entry<WatchlistNavKey> {
        WatchlistRoute()
    }
}
