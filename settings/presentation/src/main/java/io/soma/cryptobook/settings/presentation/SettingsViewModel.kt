package io.soma.cryptobook.settings.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.core.domain.model.AppTheme
import io.soma.cryptobook.core.domain.model.CurrencyUnit
import io.soma.cryptobook.core.domain.model.Language
import io.soma.cryptobook.core.domain.model.UserData
import io.soma.cryptobook.core.domain.usecase.GetUserDataUseCase
import io.soma.cryptobook.core.domain.usecase.SetLanguageUseCase
import io.soma.cryptobook.core.ui.BaseViewModel
import io.soma.cryptobook.settings.domain.usecase.SetAppThemeUseCase
import io.soma.cryptobook.settings.domain.usecase.SetPriceCurrencyUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Settings screen.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    getUserDataUseCase: GetUserDataUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val setPriceCurrencyUseCase: SetPriceCurrencyUseCase,
    private val setAppThemeUseCase: SetAppThemeUseCase,
) : BaseViewModel<SettingsState, SettingsEvent, SettingsAction>(
    initialState = SettingsState(),
) {

    init {
        getUserDataUseCase()
            .map { SettingsAction.Internal.ReceiveUserData(userData = it) }
            .onEach(::sendAction)
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.AppThemeSelect -> handleAppThemeSelect(action)
            is SettingsAction.CurrencyUnitSelect -> handleCurrencyUnitSelect(action)
            is SettingsAction.LanguageSelect -> handleLanguageSelect(action)
            is SettingsAction.Internal.ReceiveUserData -> handleReceiveUserData(action)
        }
    }

    private fun handleAppThemeSelect(action: SettingsAction.AppThemeSelect) {
        viewModelScope.launch { setAppThemeUseCase(action.appTheme) }
    }

    private fun handleCurrencyUnitSelect(action: SettingsAction.CurrencyUnitSelect) {
        viewModelScope.launch { setPriceCurrencyUseCase(action.currencyUnit) }
    }

    private fun handleLanguageSelect(action: SettingsAction.LanguageSelect) {
        viewModelScope.launch { setLanguageUseCase(action.language) }
    }

    private fun handleReceiveUserData(action: SettingsAction.Internal.ReceiveUserData) {
        mutableStateFlow.update {
            it.copy(userData = action.userData, isLoading = false)
        }
    }
}

/**
 * State for the Settings screen.
 */
data class SettingsState(val userData: UserData? = null, val isLoading: Boolean = true)

/**
 * One-shot UI events for the Settings screen. The screen currently has none.
 */
sealed class SettingsEvent

/**
 * User and system actions for the Settings screen.
 */
sealed class SettingsAction {
    data class AppThemeSelect(val appTheme: AppTheme) : SettingsAction()

    data class CurrencyUnitSelect(val currencyUnit: CurrencyUnit) : SettingsAction()

    data class LanguageSelect(val language: Language) : SettingsAction()

    /**
     * Internal actions dispatched by the ViewModel from coroutines.
     */
    sealed class Internal : SettingsAction() {
        data class ReceiveUserData(val userData: UserData) : Internal()
    }
}
