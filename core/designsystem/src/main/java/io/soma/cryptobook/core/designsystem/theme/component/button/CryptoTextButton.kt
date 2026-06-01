package io.soma.cryptobook.core.designsystem.theme.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.soma.cryptobook.core.designsystem.theme.component.button.color.cryptoTextButtonColors
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme
import io.soma.cryptobook.core.designsystem.theme.util.throttledClick

@Composable
fun CryptoTextButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = CbTheme.colorScheme.outlineButton.foreground,
) {
    TextButton(
        modifier = modifier,
        onClick = throttledClick(onClick = onClick),
        contentPadding = PaddingValues(
            horizontal = 12.dp,
            vertical = 10.dp,
        ),
        colors = cryptoTextButtonColors(contentColor = contentColor)
    ) {
        Text(
            text = label,
            style = CbTheme.typography.labelLarge,
            modifier = Modifier
        )
    }
}