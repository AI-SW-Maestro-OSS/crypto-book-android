package io.soma.cryptobook.coindetail.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.soma.cryptobook.coindetail.presentation.CoinDetailScreen

fun EntryProviderScope<NavKey>.coinDetailEntry(onNavigateBack: () -> Unit) {
    entry<CoinDetailNavKey> { key ->
        CoinDetailScreen(
            coinName = key.coinName,
            onNavigateBack = onNavigateBack,
        )
    }
}
