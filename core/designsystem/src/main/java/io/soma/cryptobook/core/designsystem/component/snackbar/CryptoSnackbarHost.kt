package io.soma.cryptobook.core.designsystem.component.snackbar

import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.soma.cryptobook.core.designsystem.component.snackbar.model.CryptoSnackbarHostState

@Composable
fun CryptoSnackbarHost(
    cryptoSnackbarHostState: CryptoSnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = cryptoSnackbarHostState.snackbarHostState,
        modifier = modifier,
    )
}