package io.soma.cryptobook.search.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.soma.cryptobook.search.presentation.SearchRoute

fun EntryProviderScope<NavKey>.searchEntry(
    onBack: () -> Unit,
    onCoinClick: (String) -> Unit,
) {
    entry<SearchNavKey> {
        SearchRoute(
            onBack = onBack,
            onCoinClick = onCoinClick,
        )
    }
}
