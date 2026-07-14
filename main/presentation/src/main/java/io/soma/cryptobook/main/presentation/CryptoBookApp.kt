package io.soma.cryptobook.main.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
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
import io.soma.cryptobook.main.presentation.message.MessageCommand
import io.soma.cryptobook.main.presentation.message.MessageCommandSource
import io.soma.cryptobook.main.presentation.navigation.CryptoNavigator
import io.soma.cryptobook.main.presentation.navigation.CryptoTopLevelNavItem
import io.soma.cryptobook.main.presentation.navigation.LinkRouter
import io.soma.cryptobook.main.presentation.navigation.NavCommand
import io.soma.cryptobook.main.presentation.navigation.NavCommandSource
import io.soma.cryptobook.navigation.NavigationState
import io.soma.cryptobook.navigation.rememberNavigationState
import io.soma.cryptobook.search.presentation.navigation.SearchNavKey
import io.soma.cryptobook.search.presentation.navigation.searchEntry
import io.soma.cryptobook.settings.presentation.navigation.settingsEntry
import io.soma.cryptobook.watchlist.presentation.navigation.watchlistEntry
import kotlinx.collections.immutable.toImmutableList

@Composable
fun CryptoBookApp(
    navSource: NavCommandSource,
    messageSource: MessageCommandSource,
    linkRouter: LinkRouter,
    appLinkKey: NavKey,
    modifier: Modifier = Modifier,
) {
    // navigation
    val navigationState = rememberNavigationState(
        startKey = HomeNavKey,
        topLevelKeys = CryptoTopLevelNavItem.entries.map { it.navKey }.toSet(),
    )
    if (appLinkKey !is HomeNavKey) {
        navigationState.backStack.add(appLinkKey)
    }
    NavigationTrackingSideEffect(navigationState)
    val navigator = remember { CryptoNavigator(navigationState) }

    // message (global loading + toast only; snackbar is handled per-screen)
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        navSource.commands.collect { cmd ->
            when (cmd) {
                is NavCommand.Navigate -> {
                    val key = linkRouter.resolve(cmd.page)
                    navigator.navigateTo(key)
                }

                // onNewIntent 상황에서만 호출됨
                is NavCommand.DeepLink -> {
                    val key = linkRouter.resolve(cmd.link)
                    navigator.popWhileAndPush(key) { it::class == key::class }
                }

                is NavCommand.Back -> {
                    navigator.goBack()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        messageSource.commands.collect { cmd ->
            when (cmd) {
                is MessageCommand.ShowLoading -> isLoading = true

                is MessageCommand.HideLoading -> isLoading = false

                is MessageCommand.ShowToast -> {
                    Toast.makeText(
                        context,
                        cmd.text.toString(context.resources),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

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

    Box(modifier = modifier.fillMaxSize()) {
        CryptoScaffold(
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
                    coinDetailEntry(navigator::goBack)
                    searchEntry(
                        onBack = navigator::goBack,
                        onCoinClick = { coinName ->
                            navigator.navigateTo(CoinDetailNavKey(coinName))
                        },
                    )
                },
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) { },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.Black)
            }
        }
    }
}

@Composable
private fun NavigationTrackingSideEffect(navigationState: NavigationState) {
    TrackDisposableJank(navigationState.currentTopKey) { metricsHolder ->
        metricsHolder.state?.putState("Navigation", navigationState.currentTopKey.toString())
        onDispose {}
    }
}
