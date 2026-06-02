package io.soma.cryptobook.core.designsystem.component.scaffold.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

data class CbPullToRefreshState(
    val isEnabled: Boolean,
    val isRefreshing: Boolean,
    val onRefresh: () -> Unit,
)

@Composable
fun rememberCbPullToRefreshState(
    isEnabled: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = { },
): CbPullToRefreshState = remember(isEnabled, isRefreshing, onRefresh) {
    CbPullToRefreshState(
        isEnabled = isEnabled,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    )
}