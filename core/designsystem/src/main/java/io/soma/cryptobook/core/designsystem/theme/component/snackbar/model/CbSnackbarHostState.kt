package io.soma.cryptobook.core.designsystem.theme.component.snackbar.model

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
data class CbSnackbarHostState(
    val snackbarHostState: SnackbarHostState,
    val scope: CoroutineScope,
) {
    fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onActionPerformed: () -> Unit = { },
        onDismiss: () -> Unit = { },
    ) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = duration,
            )
            when (result) {
                SnackbarResult.Dismissed -> onDismiss()
                SnackbarResult.ActionPerformed -> onActionPerformed()
            }
        }
    }
}

@Composable
fun rememberCbSnackbarHostState(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    scope: CoroutineScope = rememberCoroutineScope(),
): CbSnackbarHostState = remember {
    CbSnackbarHostState(snackbarHostState = snackbarHostState, scope = scope)
}