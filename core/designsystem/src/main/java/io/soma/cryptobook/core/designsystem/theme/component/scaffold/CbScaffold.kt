package io.soma.cryptobook.core.designsystem.theme.component.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.soma.cryptobook.core.designsystem.theme.component.navigation.CbBottomAppBar
import io.soma.cryptobook.core.designsystem.theme.component.navigation.CbnavigationRail
import io.soma.cryptobook.core.designsystem.theme.component.scaffold.model.CbPullToRefreshState
import io.soma.cryptobook.core.designsystem.theme.component.scaffold.model.ScaffoldNavigationData
import io.soma.cryptobook.core.designsystem.theme.component.scaffold.model.rememberCbPullToRefreshState
import io.soma.cryptobook.core.designsystem.theme.model.WindowSize
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme
import io.soma.cryptobook.core.designsystem.theme.util.rememberWindowSize

@Composable
fun CbScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = { },
    overlay: @Composable () -> Unit = { },
    snackbarHost: @Composable () -> Unit = { },
    navigationData: ScaffoldNavigationData? = null,
    pullToRefreshState: CbPullToRefreshState = rememberCbPullToRefreshState(),
    containerColor: Color = CbTheme.colorScheme.background.primary,
    contentColor: Color = CbTheme.colorScheme.text.primary,
    contentWindowInsets: WindowInsets = ScaffoldDefaults
        .contentWindowInsets
        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Horizontal),
    content: @Composable () -> Unit,
) {
    val windowSize = rememberWindowSize()
    val hasNavigationItems = !navigationData?.navigationItems.isNullOrEmpty()
    val isNavigationRailVisible = windowSize != WindowSize.Compact && hasNavigationItems
    val isNavigationBarVisible = windowSize == WindowSize.Compact && hasNavigationItems
    Scaffold(
        modifier = Modifier
            .then(other = Modifier),
        topBar = {
            Box(
                modifier = Modifier
            ) {
                topBar()
            }
        },
        bottomBar = {
            if (isNavigationBarVisible) {
                ScaffoldBottomAppBar(
                    navigationData = navigationData,
                    modifier = Modifier,
                )
            }
        },
        snackbarHost = {
            Box(
                modifier = Modifier.imePadding()
            )
            snackbarHost()
        },
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = WindowInsets(0.dp),
        content = { paddingValues ->
            Row(
                modifier = Modifier
                    .padding(paddingValues = paddingValues)
                    .consumeWindowInsets(paddingValues = paddingValues)
                    .imePadding()
            ) {
                if (isNavigationRailVisible) {
                    ScaffoldNavigationRail(
                        navigationData = navigationData,
                        modifier = Modifier
                    )
                }
                Box(
                    modifier = Modifier
                        .consumeWindowInsetsForMainContent(
                            isNavigationRailVisible = isNavigationRailVisible,
                            isNavigationBarVisible = isNavigationBarVisible
                        )
                ) {
                    Column {
                        val internalPullToRefreshState = rememberPullToRefreshState()
                        Box(
                            modifier = Modifier
                                .windowInsetsPadding(insets = contentWindowInsets)
                                .pullToRefresh(
                                    state = internalPullToRefreshState,
                                    isRefreshing = pullToRefreshState.isRefreshing,
                                    onRefresh = pullToRefreshState.onRefresh,
                                    enabled = pullToRefreshState.isEnabled
                                )
                        ) {
                            content()

                            PullToRefreshDefaults.Indicator(
                                modifier = Modifier.align(Alignment.TopCenter),
                                isRefreshing = pullToRefreshState.isRefreshing,
                                state = internalPullToRefreshState,
                                containerColor = CbTheme.colorScheme.background.secondary,
                                color = CbTheme.colorScheme.icon.secondary
                            )
                        }
                    }
                    overlay()
                }
            }
        }
    )
}

@Composable
private fun ScaffoldBottomAppBar(
    navigationData: ScaffoldNavigationData,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        var appBarHeightPx by remember { mutableIntStateOf(0) }
        CbBottomAppBar(
            navigationItems = navigationData.navigationItems,
            selectedItem = navigationData.selectedNavigationItem,
            onClick = navigationData.onNavigationClick,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { appBarHeightPx = it.size.height }
        )
    }
}

@Composable
private fun ScaffoldNavigationRail(
    navigationData: ScaffoldNavigationData,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .zIndex(zIndex = 1f),
    ) {
        var appBarWidthPx by remember { mutableIntStateOf(0) }
        CbnavigationRail(
            navigationItems = navigationData.navigationItems,
            selectedItem = navigationData.selectedNavigationItem,
            onClick = navigationData.onNavigationClick,
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth()
                .onGloballyPositioned { appBarWidthPx = it.size.width }
        )
    }
}

@Composable
private fun Modifier.consumeWindowInsetsForMainContent(
    isNavigationRailVisible: Boolean,
    isNavigationBarVisible: Boolean,
): Modifier =
    if (isNavigationRailVisible) {
        consumeWindowInsets(WindowInsets.displayCutout.only(WindowInsetsSides.Start))
    } else if (isNavigationBarVisible) {
        consumeWindowInsets(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
    } else {
        this
    }