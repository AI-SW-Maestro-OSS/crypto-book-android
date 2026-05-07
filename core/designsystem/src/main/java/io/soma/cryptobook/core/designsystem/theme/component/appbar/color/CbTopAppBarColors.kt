package io.soma.cryptobook.core.designsystem.theme.component.appbar.color

import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme

@Composable
fun cbTopAppBarColors(): TopAppBarColors = TopAppBarColors(
    containerColor = CbTheme.colorScheme.background.secondary,
    scrolledContainerColor = CbTheme.colorScheme.background.secondary,
    navigationIconContentColor = CbTheme.colorScheme.icon.primary,
    titleContentColor = CbTheme.colorScheme.text.primary,
    actionIconContentColor = CbTheme.colorScheme.icon.primary,
    subtitleContentColor = CbTheme.colorScheme.text.primary
)