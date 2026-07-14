package io.soma.cryptobook.home.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.soma.cryptobook.home.presentation.HomeScreen

fun EntryProviderScope<NavKey>.homeEntry(
    onNavigateToCoinDetail: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    entry<HomeNavKey> {
        HomeScreen(
            onNavigateToCoinDetail = onNavigateToCoinDetail,
            onNavigateToSearch = onNavigateToSearch,
        )
    }
}
