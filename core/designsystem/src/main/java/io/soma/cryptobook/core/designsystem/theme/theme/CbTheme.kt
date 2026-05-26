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
import io.soma.cryptobook.core.designsystem.theme.theme.type.CryptoTypography
import io.soma.cryptobook.core.designsystem.theme.theme.type.cryptoTypography

object CbTheme {
    val colorScheme: CbColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalCbColorScheme.current

    val typography: CryptoTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalCryptoTypography.current
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
        LocalCryptoTypography provides cryptoTypography
    ) {
        MaterialTheme(
            content = content,
        )
    }
}

val LocalCbColorScheme: ProvidableCompositionLocal<CbColorScheme> =
    compositionLocalOf { darkCbColorScheme }

val LocalCryptoTypography: ProvidableCompositionLocal<CryptoTypography> =
    compositionLocalOf { cryptoTypography }