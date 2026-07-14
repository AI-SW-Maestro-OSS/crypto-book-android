package io.soma.cryptobook.search.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.soma.cryptobook.search.presentation.SearchScreen

fun EntryProviderScope<NavKey>.searchEntry(
    onNavigateBack: () -> Unit,
    onNavigateToCoinDetail: (String) -> Unit,
) {
    entry<SearchNavKey> {
        SearchScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToCoinDetail = onNavigateToCoinDetail,
        )
    }
}
