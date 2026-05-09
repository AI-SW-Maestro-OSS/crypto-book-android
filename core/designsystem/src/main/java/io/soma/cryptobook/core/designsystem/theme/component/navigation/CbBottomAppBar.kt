package io.soma.cryptobook.core.designsystem.theme.component.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.soma.cryptobook.core.designsystem.theme.component.navigation.model.NavigationItem
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme
import kotlinx.collections.immutable.ImmutableList

@Composable
fun CbBottomAppBar(
    navigationItems: ImmutableList<NavigationItem>,
    selectedItem: NavigationItem?,
    onClick: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = BottomAppBarDefaults.windowInsets,
) {
    BottomAppBar(
        containerColor = CbTheme.colorScheme.background.secondary,
        contentColor = Color.Unspecified,
        windowInsets = windowInsets,
    ) {
        navigationItems.forEach { navigationItem ->
            CbNavigationBarItem(
                labelRes = navigationItem.labelRes,
                selectedIconRes = navigationItem.iconResSelected,
                unselectedIconRes = navigationItem.iconRes,
                isSelected = selectedItem == navigationItem,
                onClick = { onClick(navigationItem) },
                modifier = Modifier,
            )
        }
    }
}