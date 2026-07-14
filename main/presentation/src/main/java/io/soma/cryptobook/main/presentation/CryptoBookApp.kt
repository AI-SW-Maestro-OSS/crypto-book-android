package io.soma.cryptobook.main.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.soma.cryptobook.coindetail.presentation.navigation.CoinDetailNavKey
import io.soma.cryptobook.coindetail.presentation.navigation.coinDetailEntry
import io.soma.cryptobook.core.designsystem.component.scaffold.CryptoScaffold
import io.soma.cryptobook.core.designsystem.component.scaffold.model.ScaffoldNavigationData
import io.soma.cryptobook.core.presentation.jank.TrackDisposableJank
import io.soma.cryptobook.diary.presentation.navigation.diaryEntry
import io.soma.cryptobook.home.presentation.navigation.HomeNavKey
import io.soma.cryptobook.home.presentation.navigation.homeEntry
import io.soma.cryptobook.main.presentation.navigation.CryptoNavigator
import io.soma.cryptobook.main.presentation.navigation.CryptoTopLevelNavItem
import io.soma.cryptobook.navigation.NavigationState
import io.soma.cryptobook.navigation.rememberNavigationState
import io.soma.cryptobook.search.presentation.navigation.SearchNavKey
import io.soma.cryptobook.search.presentation.navigation.searchEntry
import io.soma.cryptobook.settings.presentation.navigation.settingsEntry
import io.soma.cryptobook.watchlist.presentation.navigation.watchlistEntry
import kotlinx.collections.immutable.toImmutableList

@Composable
fun CryptoBookApp(modifier: Modifier = Modifier) {
    val navigationState = rememberNavigationState(
        startKey = HomeNavKey,
        topLevelKeys = CryptoTopLevelNavItem.entries.map { it.navKey }.toSet(),
    )
    NavigationTrackingSideEffect(navigationState)
    val navigator = remember { CryptoNavigator(navigationState) }

    val selectedNavItem = CryptoTopLevelNavItem.entries
        .find { it.navKey == navigationState.currentTopKey }
    val navigationData = selectedNavItem?.let { selected ->
        ScaffoldNavigationData(
            onNavigationClick = { item ->
                if (item is CryptoTopLevelNavItem) navigator.navigateTo(item.navKey)
            },
            navigationItems = CryptoTopLevelNavItem.entries.toImmutableList(),
            selectedNavigationItem = selected,
        )
    }

    CryptoScaffold(
        modifier = modifier.fillMaxSize(),
        navigationData = navigationData,
        contentWindowInsets = WindowInsets(0),
    ) {
        NavDisplay(
            backStack = navigationState.backStack,
            onBack = { navigator.goBack() },
            modifier = Modifier.fillMaxSize(),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                settingsEntry()
                homeEntry(
                    onNavigateToCoinDetail = { symbol ->
                        navigator.navigateTo(CoinDetailNavKey(symbol))
                    },
                    onNavigateToSearch = { navigator.navigateTo(SearchNavKey) },
                )
                watchlistEntry(
                    onNavigateToCoinDetail = { symbol ->
                        navigator.navigateTo(CoinDetailNavKey(symbol))
                    },
                )
                diaryEntry()
                coinDetailEntry(onNavigateBack = navigator::goBack)
                searchEntry(
                    onNavigateBack = navigator::goBack,
                    onNavigateToCoinDetail = { coinName ->
                        navigator.navigateTo(CoinDetailNavKey(coinName))
                    },
                )
            },
        )
    }
}

@Composable
private fun NavigationTrackingSideEffect(navigationState: NavigationState) {
    TrackDisposableJank(navigationState.currentTopKey) { metricsHolder ->
        metricsHolder.state?.putState("Navigation", navigationState.currentTopKey.toString())
        onDispose {}
    }
}
