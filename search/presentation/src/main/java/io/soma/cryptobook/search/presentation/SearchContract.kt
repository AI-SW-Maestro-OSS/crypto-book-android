package io.soma.cryptobook.search.presentation

import io.soma.cryptobook.core.presentation.mvi.UnidirectionalViewModel

interface SearchContract {
    interface ViewModel : UnidirectionalViewModel<State, Event, Effect>

    data class State(
        val searchTerm: String = "",
        val viewState: ViewState = ViewState.Empty,
    ) {
        sealed interface ViewState {
            data object Empty : ViewState
            data class Content(
                val items: List<DisplayItem>,
            ) : ViewState
        }
    }

    sealed interface Event {
        data class OnSearchTermChanged(
            val searchTerm: String,
        ) : Event

        data class OnListItemClick(
            val coinName: String,
        ) : Event

        object OnBackClicked : Event
    }

    sealed interface Effect {
        data object NavigateBack : Effect

        data class NavigateToCoinDetail(
            val coinName: String,
        ) : Effect
    }
}

data class DisplayItem(
    val symbol: String,
    val imageUrl: String,
)