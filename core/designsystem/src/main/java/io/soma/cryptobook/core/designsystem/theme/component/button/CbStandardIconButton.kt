package io.soma.cryptobook.core.designsystem.theme.component.button

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import io.soma.cryptobook.core.designsystem.theme.component.button.color.cbStandardIconButtonColors
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme

@Composable
fun CbStandardIconButton(
    @DrawableRes vectorIconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    contentColor: Color = CbTheme.colorScheme.icon.primary,
) {
    CbStandardIconButton(
        painter = painterResource(vectorIconRes),
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        isEnabled = isEnabled,
        contentColor = contentColor,
    )
}

@Composable
fun CbStandardIconButton(
    painter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    contentColor: Color = CbTheme.colorScheme.icon.primary,
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
        colors = cbStandardIconButtonColors(contentColor = contentColor),
        enabled = isEnabled,
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
        )
    }
}