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
        /**
         * [localeTag] 가 null 이면 시스템 언어를 따른다(빈 로케일 목록 적용).
         */
        data class ApplyLocale(val localeTag: String?) : Effect
        data class ApplyTheme(val nightMode: Int) : Effect
    }
}
