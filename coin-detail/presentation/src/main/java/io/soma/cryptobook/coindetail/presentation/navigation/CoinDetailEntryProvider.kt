package io.soma.cryptobook.coindetail.presentation.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.soma.cryptobook.coindetail.presentation.CoinDetailRoute
import io.soma.cryptobook.coindetail.presentation.CoinDetailViewModel

fun EntryProviderScope<NavKey>.coinDetailEntry(onBack: () -> Unit) {
    entry<CoinDetailNavKey> { key ->
        val coinName = key.coinName
        CoinDetailRoute(
            onBack = onBack,
            viewModel = hiltViewModel(
                creationCallback = { factory: CoinDetailViewModel.Factory ->
                    factory.create(coinName)
                },
            ),
        )
    }
}
