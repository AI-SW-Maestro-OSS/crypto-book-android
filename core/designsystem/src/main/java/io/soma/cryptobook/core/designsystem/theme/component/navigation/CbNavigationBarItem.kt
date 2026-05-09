package io.soma.cryptobook.core.designsystem.theme.component.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import io.soma.cryptobook.core.designsystem.theme.component.navigation.color.cbNavigationBarItemColors
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme

@Composable
fun RowScope.CbNavigationBarItem(
    @StringRes labelRes: Int,
    @DrawableRes selectedIconRes: Int,
    @DrawableRes unselectedIconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBarItem(
        icon = {
            Box() {
                Icon(
                    painter = painterResource(
                        id = if (isSelected) selectedIconRes else unselectedIconRes,
                    ),
                    contentDescription = null,
                    tint = if (isSelected) {
                        Color.Unspecified
                    } else {
                        CbTheme.colorScheme.icon.primary
                    }
                )
            }
        },
        label = {
            val label = stringResource(id = labelRes)
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
            )
        },
        selected = isSelected,
        onClick = onClick,
        colors = cbNavigationBarItemColors(),
        modifier = modifier,
    )
}