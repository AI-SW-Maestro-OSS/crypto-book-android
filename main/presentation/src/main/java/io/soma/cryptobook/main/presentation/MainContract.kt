package io.soma.cryptobook.main.presentation

import io.soma.cryptobook.core.domain.model.Language
import io.soma.cryptobook.core.presentation.mvi.UnidirectionalViewModel

interface MainContract {
    interface ViewModel : UnidirectionalViewModel<State, Event, Effect>

    data object State

    sealed interface Event {
        data class OnSystemLocaleDetected(val language: Language) : Event
    }

    sealed interface Effect {
        data class ApplyLocale(val localeTag: String) : Effect
        data class ApplyTheme(val nightMode: Int) : Effect
    }
}
