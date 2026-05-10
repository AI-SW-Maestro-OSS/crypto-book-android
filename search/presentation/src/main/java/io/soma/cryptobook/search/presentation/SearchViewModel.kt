package io.soma.cryptobook.search.presentation

import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.core.presentation.mvi.BaseViewModel
import io.soma.cryptobook.search.presentation.SearchContract.Effect
import io.soma.cryptobook.search.presentation.SearchContract.Event
import io.soma.cryptobook.search.presentation.SearchContract.State
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(

) : BaseViewModel<State, Event, Effect>(State()) {
    override fun event(event: Event) {
        when (event) {
            is Event.OnSearchTermChanged -> updateSearchTerm(event.searchTerm)
            is Event.OnListItemClick -> navigateToCoinDetail(event.coinName)
            Event.OnBackClicked -> navigateBack()
        }
    }

    private fun updateSearchTerm(searchTerm: String) {
        updateState {
            it.copy(
                searchTerm = searchTerm,
                viewState = createViewState(searchTerm),
            )
        }
    }

    private fun createViewState(searchTerm: String): State.ViewState {
        if (searchTerm.isBlank()) {
            return State.ViewState.Empty
        }

        val items = sampleItems.filter { item ->
            item.symbol.contains(searchTerm, ignoreCase = true)
        }

        return if (items.isEmpty()) {
            State.ViewState.Empty
        } else {
            State.ViewState.Content(items = items)
        }
    }

    private fun navigateToCoinDetail(coinName: String) {
        emitEffect(Effect.NavigateToCoinDetail(coinName))
    }

    private fun navigateBack() = emitEffect(Effect.NavigateBack)

    private companion object {
        val sampleItems = listOf(
            DisplayItem(symbol = "BTCUSDT", imageUrl = ""),
            DisplayItem(symbol = "ETHUSDT", imageUrl = ""),
            DisplayItem(symbol = "BNBUSDT", imageUrl = ""),
            DisplayItem(symbol = "SOLUSDT", imageUrl = ""),
            DisplayItem(symbol = "XRPUSDT", imageUrl = ""),
        )
    }
}