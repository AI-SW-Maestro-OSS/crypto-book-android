package io.soma.cryptobook.core.designsystem.theme.component.snackbar

import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.soma.cryptobook.core.designsystem.theme.component.snackbar.model.CbSnackbarHostState

@Composable
fun CbSnackbarHost(
    cbSnackbarHostState: CbSnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = cbSnackbarHostState.snackbarHostState,
        modifier = modifier,
    )
}