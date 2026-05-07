package io.soma.cryptobook.core.designsystem.theme.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import io.soma.cryptobook.core.designsystem.theme.ScreenBackground

@Composable
fun CbTheme(
    content: @Composable () -> Unit,
) {
    val defaultBackgroundTheme = BackgroundTheme(
        color = ScreenBackground
    )

    CompositionLocalProvider(
        LocalBackgroundTheme provides defaultBackgroundTheme,
    ) {
        MaterialTheme(
            content = content,
        )
    }
}