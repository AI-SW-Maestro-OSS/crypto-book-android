package io.soma.cryptobook.search.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.soma.cryptobook.core.designsystem.component.appbar.CryptoSearchTopAppBar
import io.soma.cryptobook.core.designsystem.component.appbar.NavigationIcon
import io.soma.cryptobook.core.designsystem.component.scaffold.CryptoScaffold
import io.soma.cryptobook.core.designsystem.theme.resource.CryptoDrawable
import io.soma.cryptobook.core.presentation.mvi.observe
import io.soma.cryptobook.search.presentation.SearchContract.Effect
import io.soma.cryptobook.search.presentation.SearchContract.Event
import io.soma.cryptobook.search.presentation.SearchContract.State

@Composable
fun SearchRoute(
    viewModel: SearchViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onCoinClick: (String) -> Unit,
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
    )
}

@Composable
fun SearchScreen(state: State, onEvent: (Event) -> Unit, modifier: Modifier = Modifier) {
    CryptoScaffold(
        topBar = {
            CryptoSearchTopAppBar(
                searchTerm = state.searchTerm,
                placeholder = "Search",
                onSearchTermChange = { searchTerm ->
                    onEvent(Event.OnSearchTermChanged(searchTerm))
                },
                navigationIcon = NavigationIcon(
                    navigationIcon = painterResource(id = CryptoDrawable.ic_arrow_back),
                    navigationIconContentDescription = "Back",
                    onNavigationIconClick = { onEvent(Event.OnBackClicked) },
                ),
                clearIconContentDescription = "clear",
            )
        },
    ) {
        when (val viewState = state.viewState) {
            State.ViewState.Loading -> {
                Text("Loading..")
            }

            is State.ViewState.Empty -> SearchEmptyContent(
                viewState = viewState,
                modifier = Modifier.fillMaxSize(),
            )

            is State.ViewState.Error -> {
                Text(viewState.message)
            }

            is State.ViewState.Content -> {
                SearchContent(
                    items = viewState.items,
                    onItemClick = { symbol ->
                        onEvent(Event.OnListItemClick(symbol))
                    },
                )
            }
        }
    }
}
