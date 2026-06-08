package io.soma.cryptobook.core.designsystem.component.appbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.union
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import io.soma.cryptobook.core.designsystem.component.appbar.color.cryptoTopAppBarColors
import io.soma.cryptobook.core.designsystem.component.button.CryptoStandardIconButton
import io.soma.cryptobook.core.designsystem.theme.theme.CryptoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoMediumTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets
        .union(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)),
    navigationIcon: NavigationIcon? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CenterAlignedTopAppBar(
        windowInsets = windowInsets,
        colors = cryptoTopAppBarColors(),
        navigationIcon = {
            navigationIcon?.let {
                CryptoStandardIconButton(
                    painter = it.navigationIcon,
                    contentDescription = it.navigationIconContentDescription,
                    onClick = it.onNavigationIconClick,
                    modifier = Modifier,
                )
            }
        },
        title = {
            Text(
                text = title,
                style = CryptoTheme.typography.titleLarge,
            )
        },
        modifier = modifier,
        actions = actions,
    )
}

data class NavigationIcon(
    val navigationIcon: Painter,
    val navigationIconContentDescription: String,
    val onNavigationIconClick: () -> Unit,
)