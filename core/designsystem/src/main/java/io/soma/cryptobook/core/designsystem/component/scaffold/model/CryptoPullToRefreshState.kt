package io.soma.cryptobook.core.designsystem.component.scaffold.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

data class CryptoPullToRefreshState(
    val isEnabled: Boolean,
    val isRefreshing: Boolean,
    val onRefresh: () -> Unit,
)

@Composable
fun rememberCryptoPullToRefreshState(
    isEnabled: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = { },
): CryptoPullToRefreshState = remember(isEnabled, isRefreshing, onRefresh) {
    CryptoPullToRefreshState(
        isEnabled = isEnabled,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    )
}