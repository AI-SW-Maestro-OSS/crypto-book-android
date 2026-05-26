package io.soma.cryptobook.search.presentation

import io.soma.cryptobook.core.presentation.mvi.UnidirectionalViewModel

interface SearchContract {
    interface ViewModel : UnidirectionalViewModel<State, Event, Effect>

    data class State(
        val searchTerm: String = "",
        val viewState: ViewState = ViewState.Empty(message = null),
    ) {
        sealed interface ViewState {
            data object Loading : ViewState
            data class Empty(val message: String?) : ViewState
            data class Content(val items: List<DisplayItem>) : ViewState

            data class Error(val message: String) : ViewState
        }
    }

    sealed interface Event {
        data class OnSearchTermChanged(val searchTerm: String) : Event

        data class OnListItemClick(val coinName: String) : Event

        object OnBackClicked : Event
    }

    sealed interface Effect {
        data object NavigateBack : Effect

        data class NavigateToCoinDetail(val coinName: String) : Effect
    }
}

data class DisplayItem(val symbol: String, val imageUrl: String)
