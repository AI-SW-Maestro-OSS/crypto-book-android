package io.soma.cryptobook.core.designsystem.theme.component.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.soma.cryptobook.core.designsystem.theme.component.navigation.model.NavigationItem
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme
import kotlinx.collections.immutable.ImmutableList

@Composable
fun CbNavigationRail(
    navigationItems: ImmutableList<NavigationItem>,
    selectedItem: NavigationItem?,
    onClick: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = NavigationRailDefaults.windowInsets
        .union(WindowInsets.displayCutout.only(WindowInsetsSides.Start)),
) {
    val density = LocalDensity.current
    Surface(
        color = CbTheme.colorScheme.background.secondary,
        contentColor = Color.Unspecified,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .windowInsetsPadding(insets = windowInsets)
                .widthIn(min = 80.dp)
                .padding(all = 4.dp)
                .verticalScroll(state = rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = 16.dp,
                alignment = Alignment.CenterVertically
            )
        ) {
            navigationItems.forEach { navigationItem ->
                CbNavigationRailItem(
                    labelRes = navigationItem.labelRes,
                    selectedIconRes = navigationItem.iconResSelected,
                    unselectedIconRes = navigationItem.iconRes,
                    isSelected = navigationItem == selectedItem,
                    onClick = { onClick(navigationItem) },
                    modifier = Modifier,
                )
            }
        }
    }
}