package io.soma.cryptobook.core.designsystem.theme.component.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import io.soma.cryptobook.core.designsystem.theme.component.navigation.color.cbNavigationRailItemColors

@Composable
fun CbNavigationRailItem(
    @StringRes labelRes: Int,
    @DrawableRes selectedIconRes: Int,
    @DrawableRes unselectedIconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRailItem(
        icon = {
            Box() {
                Icon(
                    painter = painterResource(
                        id = if (isSelected) selectedIconRes else unselectedIconRes,
                    ),
                    contentDescription = null,
                )
            }
        },
        label = {
            val label = stringResource(id = labelRes)
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier,
            )
        },
        selected = isSelected,
        onClick = onClick,
        modifier = modifier,
        colors = cbNavigationRailItemColors(),
    )
}