package io.soma.cryptobook.diary.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.designsystem.theme.component.appbar.CbMediumTopAppBar
import io.soma.cryptobook.core.designsystem.theme.component.scaffold.CbScaffold
import io.soma.cryptobook.core.designsystem.theme.theme.CbTheme
import io.soma.cryptobook.core.presentation.mvi.observeWithoutEffect

@Composable
fun DiaryRoute(modifier: Modifier = Modifier, viewModel: DiaryViewModel = hiltViewModel()) {
    val (state, dispatch) = viewModel.observeWithoutEffect()

    CbScaffold(
        modifier = modifier,
        topBar = {
            CbMediumTopAppBar(
                title = stringResource(CryptoString.cb_top_level_diary),
            )
        },
    ) {
        DiaryScreen(
            state = state.value,
            onEvent = dispatch,
        )
    }
}

@Composable
internal fun DiaryScreen(
    state: DiaryContract.State,
    onEvent: (DiaryContract.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CbTheme.colorScheme.background.secondary),
    )
}

@Preview(showBackground = true)
@Composable
private fun DiaryScreenPreview() {
    DiaryScreen(
        state = DiaryContract.State(),
        onEvent = {},
    )
}
