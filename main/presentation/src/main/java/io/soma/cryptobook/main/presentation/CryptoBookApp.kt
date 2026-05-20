package io.soma.cryptobook.main.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.soma.cryptobook.coindetail.presentation.navigation.CoinDetailNavKey
import io.soma.cryptobook.coindetail.presentation.navigation.coinDetailEntry
import io.soma.cryptobook.core.designsystem.theme.cbNavigationItemColors
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme
import io.soma.cryptobook.home.presentation.navigation.HomeNavKey
import io.soma.cryptobook.home.presentation.navigation.homeEntry
import io.soma.cryptobook.main.presentation.message.MessageCommand
import io.soma.cryptobook.main.presentation.message.MessageCommandSource
import io.soma.cryptobook.main.presentation.navigation.CbNavigator
import io.soma.cryptobook.main.presentation.navigation.LinkRouter
import io.soma.cryptobook.main.presentation.navigation.NavCommand
import io.soma.cryptobook.main.presentation.navigation.NavCommandSource
import io.soma.cryptobook.main.presentation.navigation.TOP_LEVEL_NAV_ITEMS
import io.soma.cryptobook.navigation.rememberNavigationState
import io.soma.cryptobook.search.presentation.navigation.searchEntry
import io.soma.cryptobook.settings.presentation.navigation.settingsEntry

@Composable
fun CryptoBookApp(
    navSource: NavCommandSource,
    messageSource: MessageCommandSource,
    linkRouter: LinkRouter,
    appLinkKey: NavKey,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
    modifier: Modifier = Modifier,
) {
    val navigationSuiteItemColors = cbNavigationItemColors()
    val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(windowAdaptiveInfo)

    // navigation
    val navigationState = rememberNavigationState(HomeNavKey, TOP_LEVEL_NAV_ITEMS.keys)
    if (appLinkKey !is HomeNavKey) {
        navigationState.backStack.add(appLinkKey)
    }
    val navigator = remember { CbNavigator(navigationState) }

    // message
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
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
                    Toast.makeText(context, cmd.message, Toast.LENGTH_SHORT).show()
                }

                is MessageCommand.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = cmd.message,
                        actionLabel = cmd.actionLabel,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        cmd.onAction?.invoke()
                    }
                }

                is MessageCommand.DismissSnackbar -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
            }
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            TOP_LEVEL_NAV_ITEMS.forEach { (navKey, navItem) ->
                val selected =
                    navKey == navigationState.currentTopKey
                item(
                    selected = selected,
                    icon = { Icon(navItem.icon, contentDescription = null) },
                    label = { Text(stringResource(navItem.iconTextId)) },
                    onClick = { navigator.navigateTo(navKey) },
                    colors = navigationSuiteItemColors,
                )
            }
        },
        layoutType = layoutType,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = CbTheme.colorScheme.background.primary,
            navigationRailContainerColor = CbTheme.colorScheme.background.primary,
        ),
    ) {
        Scaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { padding ->
            NavDisplay(
                backStack = navigationState.backStack,
                onBack = { navigator.goBack() },
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding),
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    settingsEntry()
                    homeEntry()
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
