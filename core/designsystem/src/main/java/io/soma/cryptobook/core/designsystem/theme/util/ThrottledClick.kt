package io.soma.cryptobook.core.designsystem.theme.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun throttledClick(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    delayMs: Long = 300,
    onClick: () -> Unit,
): () -> Unit {
    var isEnabled by remember { mutableStateOf(value = true) }
    return {
        coroutineScope.launch {
            if (isEnabled) {
                isEnabled = false
                onClick()
                delay(timeMillis = delayMs)
                isEnabled = true
            }
        }
    }
}