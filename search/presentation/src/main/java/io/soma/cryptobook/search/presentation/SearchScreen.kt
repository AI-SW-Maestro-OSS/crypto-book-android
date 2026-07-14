package io.soma.cryptobook.search.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.soma.cryptobook.core.designsystem.component.appbar.CryptoSearchTopAppBar
import io.soma.cryptobook.core.designsystem.component.appbar.NavigationIcon
import io.soma.cryptobook.core.designsystem.component.scaffold.CryptoScaffold
import io.soma.cryptobook.core.designsystem.theme.resource.CryptoDrawable
import io.soma.cryptobook.core.ui.EventsEffect

/**
 * The Search screen.
 */
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCoinDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel = viewModel) { event ->
        when (event) {
            SearchEvent.NavigateBack -> onNavigateBack()
            is SearchEvent.NavigateToCoinDetail -> onNavigateToCoinDetail(event.coinName)
        }
    }

    BackHandler {
        viewModel.trySendAction(SearchAction.BackClick)
    }

    CryptoScaffold(
        modifier = modifier,
        topBar = {
            CryptoSearchTopAppBar(
                searchTerm = state.searchTerm,
                placeholder = "Search",
                onSearchTermChange = { searchTerm ->
                    viewModel.trySendAction(SearchAction.SearchTermChange(searchTerm))
                },
                navigationIcon = NavigationIcon(
                    navigationIcon = painterResource(id = CryptoDrawable.ic_arrow_back),
                    navigationIconContentDescription = "Back",
                    onNavigationIconClick = {
                        viewModel.trySendAction(SearchAction.BackClick)
                    },
                ),
                clearIconContentDescription = "clear",
            )
        },
    ) {
        SearchScreenContent(
            state = state,
            onItemClick = { coinName -> viewModel.trySendAction(SearchAction.ItemClick(coinName)) },
        )
    }
}

@Composable
internal fun SearchScreenContent(
    state: SearchState,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val viewState = state.viewState) {
        SearchState.ViewState.Loading -> {
            Text("Loading..", modifier = modifier)
        }

        is SearchState.ViewState.Empty -> SearchEmptyContent(
            viewState = viewState,
            modifier = modifier.fillMaxSize(),
        )

        is SearchState.ViewState.Error -> {
            Text(viewState.message, modifier = modifier)
        }

        is SearchState.ViewState.Content -> {
            SearchContent(
                items = viewState.items,
                onItemClick = onItemClick,
                modifier = modifier,
            )
        }
    }
}
