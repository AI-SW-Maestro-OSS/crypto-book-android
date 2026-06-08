package io.soma.cryptobook.core.designsystem.theme.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import io.soma.cryptobook.core.designsystem.theme.theme.color.CryptoColorScheme
import io.soma.cryptobook.core.designsystem.theme.theme.color.darkCryptoColorScheme
import io.soma.cryptobook.core.designsystem.theme.theme.color.lightCryptoColorScheme
import io.soma.cryptobook.core.designsystem.theme.theme.type.CryptoTypography
import io.soma.cryptobook.core.designsystem.theme.theme.type.cryptoTypography

object CryptoTheme {
    val colorScheme: CryptoColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalCryptoColorScheme.current

    val typography: CryptoTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalCryptoTypography.current
}

@Composable
fun CryptoTheme(
    content: @Composable () -> Unit,
) {
    val cryptoColorScheme = if (isSystemInDarkTheme()) {
        darkCryptoColorScheme
    } else {
        lightCryptoColorScheme
    }

    CompositionLocalProvider(
        LocalCryptoColorScheme provides cryptoColorScheme,
        LocalCryptoTypography provides cryptoTypography
    ) {
        MaterialTheme(
            content = content,
        )
    }
}

val LocalCryptoColorScheme: ProvidableCompositionLocal<CryptoColorScheme> =
    compositionLocalOf { darkCryptoColorScheme }

val LocalCryptoTypography: ProvidableCompositionLocal<CryptoTypography> =
    compositionLocalOf { cryptoTypography }