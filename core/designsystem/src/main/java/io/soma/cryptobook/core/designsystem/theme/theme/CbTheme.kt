package io.soma.cryptobook.core.designsystem.theme.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import io.soma.cryptobook.core.designsystem.theme.theme.color.CbColorScheme
import io.soma.cryptobook.core.designsystem.theme.theme.color.darkCbColorScheme
import io.soma.cryptobook.core.designsystem.theme.theme.color.lightCbColorScheme

object CbTheme {
    val colorScheme: CbColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalCbColorScheme.current
}

@Composable
fun CbTheme(
    content: @Composable () -> Unit,
) {
    val cbColorScheme = if (isSystemInDarkTheme()) {
        darkCbColorScheme
    } else {
        lightCbColorScheme
    }

    val defaultBackgroundTheme = BackgroundTheme(
        color = cbColorScheme.background.primary
    )

    CompositionLocalProvider(
        LocalBackgroundTheme provides defaultBackgroundTheme,
        LocalCbColorScheme provides cbColorScheme,
    ) {
        MaterialTheme(
            content = content,
        )
    }
}

val LocalCbColorScheme: ProvidableCompositionLocal<CbColorScheme> =
    compositionLocalOf { darkCbColorScheme }