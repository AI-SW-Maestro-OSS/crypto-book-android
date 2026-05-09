package io.soma.cryptobook.core.designsystem.theme.util

import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.soma.cryptobook.core.designsystem.theme.model.WindowSize

@Composable
fun rememberWindowSize(
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
): WindowSize {
    return remember(key1 = windowAdaptiveInfo.windowSizeClass) {
        windowAdaptiveInfo.getWindowSize()
    }
}

fun WindowAdaptiveInfo.getWindowSize(): WindowSize {
    return if (this.windowSizeClass.isWidthAtLeastBreakpoint(widthDpBreakpoint = 600)) {
        WindowSize.Medium
    } else {
        WindowSize.Compact
    }
}