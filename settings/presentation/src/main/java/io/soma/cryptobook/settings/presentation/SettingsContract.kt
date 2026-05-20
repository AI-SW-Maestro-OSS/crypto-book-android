package io.soma.cryptobook.settings.presentation

import io.soma.cryptobook.core.domain.model.AppTheme
import io.soma.cryptobook.core.domain.model.CurrencyUnit
import io.soma.cryptobook.core.domain.model.Language
import io.soma.cryptobook.core.domain.model.UserData
import io.soma.cryptobook.core.presentation.mvi.UnidirectionalViewModel

interface SettingsContract {
    interface ViewModel : UnidirectionalViewModel<State, Event, Effect>

    data class State(
        val userData: UserData? = null,
        val isLoading: Boolean = true,
    )

    sealed interface Event {
        data class SetLanguage(val language: Language) : Event
        data class SetCurrencyUnit(val currencyUnit: CurrencyUnit) : Event
        data class SetAppTheme(val appTheme: AppTheme) : Event
        data object NavigateToHome : Event
        data object ShowLoadingMessage : Event
        data object ShowSnackbarMessage : Event
    }

    sealed interface Effect
}
