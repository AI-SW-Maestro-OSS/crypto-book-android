package io.soma.cryptobook.core.designsystem.component.appbar.color

import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import io.soma.cryptobook.core.designsystem.theme.theme.CryptoTheme

@Composable
fun cryptoTopAppBarColors(): TopAppBarColors = TopAppBarColors(
    containerColor = CryptoTheme.colorScheme.background.secondary,
    scrolledContainerColor = CryptoTheme.colorScheme.background.secondary,
    navigationIconContentColor = CryptoTheme.colorScheme.icon.primary,
    titleContentColor = CryptoTheme.colorScheme.text.primary,
    actionIconContentColor = CryptoTheme.colorScheme.icon.primary,
    subtitleContentColor = CryptoTheme.colorScheme.text.primary
)