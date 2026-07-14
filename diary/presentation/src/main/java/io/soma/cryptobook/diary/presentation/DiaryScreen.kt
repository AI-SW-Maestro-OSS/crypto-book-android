package io.soma.cryptobook.diary.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.soma.cryptobook.core.designsystem.component.appbar.CryptoMediumTopAppBar
import io.soma.cryptobook.core.designsystem.component.scaffold.CryptoScaffold
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.theme.CryptoTheme

/**
 * The Diary screen.
 */
@Composable
fun DiaryScreen(modifier: Modifier = Modifier, viewModel: DiaryViewModel = hiltViewModel()) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    CryptoScaffold(
        modifier = modifier,
        topBar = {
            CryptoMediumTopAppBar(
                title = stringResource(CryptoString.crypto_top_level_diary),
            )
        },
    ) {
        DiaryScreenContent(state = state)
    }
}

@Composable
internal fun DiaryScreenContent(state: DiaryState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoTheme.colorScheme.background.secondary),
    )
}

@Preview(showBackground = true)
@Composable
private fun DiaryScreenContentPreview() {
    DiaryScreenContent(state = DiaryState())
}
