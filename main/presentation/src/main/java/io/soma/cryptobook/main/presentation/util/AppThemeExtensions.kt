package io.soma.cryptobook.main.presentation.util

import androidx.appcompat.app.AppCompatDelegate
import io.soma.cryptobook.core.domain.model.AppTheme

fun AppTheme.toNightMode(): Int = when (this) {
    AppTheme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
    AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
}
