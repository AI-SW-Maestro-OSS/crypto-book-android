package io.soma.cryptobook.search.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.soma.cryptobook.core.designsystem.theme.component.appbar.CbSearchTopAppBar
import io.soma.cryptobook.core.designsystem.theme.component.appbar.NavigationIcon
import io.soma.cryptobook.core.designsystem.theme.component.scaffold.CbScaffold
import io.soma.cryptobook.core.designsystem.theme.resource.CbDrawable
import io.soma.cryptobook.core.presentation.mvi.observe
import io.soma.cryptobook.search.presentation.SearchContract.Effect
import io.soma.cryptobook.search.presentation.SearchContract.Event
import io.soma.cryptobook.search.presentation.SearchContract.State

@Composable
fun SearchRoute(
    viewModel: SearchViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onCoinClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (state, dispatch) = viewModel.observe { effect ->
        when (effect) {
            Effect.NavigateBack -> onBack()
            is Effect.NavigateToCoinDetail -> {
                onCoinClick(effect.coinName)
            }
        }
    }

    BackHandler {
        dispatch(Event.OnBackClicked)
    }

    SearchScreen(
        state = state.value,
        onEvent = dispatch,
        modifier = modifier
    )
}

@Composable
fun SearchScreen(
    state: State,
    onEvent: (Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    CbScaffold(
        topBar = {
            CbSearchTopAppBar(
                searchTerm = state.searchTerm,
                placeholder = "Search",
                onSearchTermChange = { searchTerm ->
                    onEvent(Event.OnSearchTermChanged(searchTerm))
                },
                navigationIcon = NavigationIcon(
                    navigationIcon = painterResource(id = CbDrawable.ic_arrow_back),
                    navigationIconContentDescription = "Back",
                    onNavigationIconClick = { onEvent(Event.OnBackClicked) },
                ),
                clearIconContentDescription = "clear",
            )
        }
    ) {
        when (val viewState = state.viewState) {
            State.ViewState.Empty -> {
                Text("Empty")
            }

            is State.ViewState.Content -> {
                SearchContent(
                    items = viewState.items,
                    onItemClick = { coinName ->
                        onEvent(Event.OnListItemClick(coinName))
                    }
                )
            }
        }
    }
}