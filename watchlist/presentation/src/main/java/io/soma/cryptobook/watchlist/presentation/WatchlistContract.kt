package io.soma.cryptobook.watchlist.presentation

import io.soma.cryptobook.core.presentation.mvi.UnidirectionalViewModel

interface WatchlistContract {
    interface ViewModel : UnidirectionalViewModel<State, Event, Effect>

    data class State(val isLoading: Boolean = false)

    sealed interface Event

    sealed interface Effect
}
