package io.soma.cryptobook.search.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.core.domain.error.CoinPriceError
import io.soma.cryptobook.core.domain.image.CoinImageResolver
import io.soma.cryptobook.core.domain.outcome.Outcome
import io.soma.cryptobook.core.domain.outcome.handle
import io.soma.cryptobook.core.ui.BaseViewModel
import io.soma.cryptobook.search.domain.model.SearchCoin
import io.soma.cryptobook.search.domain.usecase.FilterSearchCoinsUseCase
import io.soma.cryptobook.search.domain.usecase.ObserveSearchCoinsUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for the Search screen.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    observeSearchCoinsUseCase: ObserveSearchCoinsUseCase,
    private val filterSearchCoinsUseCase: FilterSearchCoinsUseCase,
    private val coinImageResolver: CoinImageResolver,
) : BaseViewModel<SearchState, SearchEvent, SearchAction>(
    initialState = SearchState(),
) {

    init {
        combine(
            stateFlow
                .map { it.searchTerm }
                .distinctUntilChanged(),
            observeSearchCoinsUseCase(),
        ) { searchTerm, outcome ->
            SearchAction.Internal.ReceiveSearchCoins(searchTerm = searchTerm, outcome = outcome)
        }
            .onEach(::sendAction)
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: SearchAction) {
        when (action) {
            SearchAction.BackClick -> handleBackClick()
            is SearchAction.ItemClick -> handleItemClick(action)
            is SearchAction.SearchTermChange -> handleSearchTermChange(action)
            is SearchAction.Internal.ReceiveSearchCoins -> handleReceiveSearchCoins(action)
        }
    }

    private fun handleBackClick() {
        sendEvent(SearchEvent.NavigateBack)
    }

    private fun handleItemClick(action: SearchAction.ItemClick) {
        sendEvent(SearchEvent.NavigateToCoinDetail(coinName = action.coinName))
    }

    private fun handleSearchTermChange(action: SearchAction.SearchTermChange) {
        mutableStateFlow.update { it.copy(searchTerm = action.searchTerm) }
    }

    private fun handleReceiveSearchCoins(action: SearchAction.Internal.ReceiveSearchCoins) {
        action.outcome.handle(
            onSuccess = { coins ->
                val viewState = createViewState(searchTerm = action.searchTerm, coins = coins)
                mutableStateFlow.update { it.copy(viewState = viewState) }
            },
            onFailure = {
                mutableStateFlow.update {
                    it.copy(
                        viewState = SearchState.ViewState.Error(
                            message = "코인 목록을 불러오지 못했습니다.",
                        ),
                    )
                }
            },
        )
    }

    private fun createViewState(
        searchTerm: String,
        coins: List<SearchCoin>,
    ): SearchState.ViewState {
        val trimmedSearchTerm = searchTerm.trim()

        if (trimmedSearchTerm.isBlank()) {
            return SearchState.ViewState.Empty(message = null)
        }
        val items = filterSearchCoinsUseCase(
            searchTerm = searchTerm,
            coins = coins,
        ).map { coin ->
            DisplayItem(
                symbol = coin.symbol,
                imageUrl = coinImageResolver.getImageUrl(coin.symbol),
            )
        }

        return if (items.isEmpty()) {
            SearchState.ViewState.Empty(message = "There's no result")
        } else {
            SearchState.ViewState.Content(items = items)
        }
    }
}

/**
 * State for the Search screen.
 */
data class SearchState(
    val searchTerm: String = "",
    val viewState: ViewState = ViewState.Empty(message = null),
) {
    /**
     * The content shown below the search bar.
     */
    sealed interface ViewState {
        data object Loading : ViewState

        data class Empty(val message: String?) : ViewState

        data class Content(val items: List<DisplayItem>) : ViewState

        data class Error(val message: String) : ViewState
    }
}

/**
 * One-shot UI events for the Search screen.
 */
sealed class SearchEvent {
    data object NavigateBack : SearchEvent()

    data class NavigateToCoinDetail(val coinName: String) : SearchEvent()
}

/**
 * User and system actions for the Search screen.
 */
sealed class SearchAction {
    data object BackClick : SearchAction()

    data class ItemClick(val coinName: String) : SearchAction()

    data class SearchTermChange(val searchTerm: String) : SearchAction()

    /**
     * Internal actions dispatched by the ViewModel from coroutines.
     */
    sealed class Internal : SearchAction() {
        data class ReceiveSearchCoins(
            val searchTerm: String,
            val outcome: Outcome<List<SearchCoin>, CoinPriceError>,
        ) : Internal()
    }
}

data class DisplayItem(val symbol: String, val imageUrl: String)
