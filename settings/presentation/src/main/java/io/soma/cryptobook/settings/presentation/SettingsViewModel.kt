package io.soma.cryptobook.settings.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.soma.cryptobook.core.designsystem.resource.CryptoString
import io.soma.cryptobook.core.domain.message.MessageHelper
import io.soma.cryptobook.core.domain.model.AppTheme
import io.soma.cryptobook.core.domain.model.CurrencyUnit
import io.soma.cryptobook.core.domain.model.Language
import io.soma.cryptobook.core.domain.navigation.AppPage
import io.soma.cryptobook.core.domain.navigation.NavigationHelper
import io.soma.cryptobook.core.domain.usecase.GetUserDataUseCase
import io.soma.cryptobook.core.domain.usecase.SetLanguageUseCase
import io.soma.cryptobook.core.presentation.mvi.BaseViewModel
import io.soma.cryptobook.settings.domain.usecase.SetAppThemeUseCase
import io.soma.cryptobook.settings.domain.usecase.SetPriceCurrencyUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val navigationHelper: NavigationHelper,
    private val messageHelper: MessageHelper,
    private val getUserDataUseCase: GetUserDataUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val setPriceCurrencyUseCase: SetPriceCurrencyUseCase,
    private val setAppThemeUseCase: SetAppThemeUseCase,
) : BaseViewModel<SettingsContract.State, SettingsContract.Event, SettingsContract.Effect>(
    SettingsContract.State(),
),
    SettingsContract.ViewModel {
    override fun event(event: SettingsContract.Event) {
        when (event) {
            is SettingsContract.Event.SetLanguage -> onLanguageChanged(event.language)

            is SettingsContract.Event.SetCurrencyUnit -> onPriceCurrencyChanged(
                event.currencyUnit,
            )

            is SettingsContract.Event.SetAppTheme -> onAppThemeChanged(event.appTheme)

            is SettingsContract.Event.NavigateToHome -> navigateToHome()

            is SettingsContract.Event.ShowLoadingMessage -> showLoadingMessage()

            is SettingsContract.Event.ShowSnackbarMessage -> showSnackbarMessage()
        }
    }

    init {
        observeUserData()
    }

    private fun observeUserData() {
        getUserDataUseCase()
            .onEach { userData ->
                updateState { state ->
                    state.copy(userData = userData, isLoading = false)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun onLanguageChanged(language: Language) = viewModelScope.launch {
        setLanguageUseCase(language)
    }

    private fun onPriceCurrencyChanged(currencyUnit: CurrencyUnit) = viewModelScope.launch {
        setPriceCurrencyUseCase(currencyUnit)
    }

    private fun navigateToHome() = viewModelScope.launch {
        navigationHelper.navigate(AppPage.Home)
    }

    private fun onAppThemeChanged(appTheme: AppTheme) = viewModelScope.launch {
        setAppThemeUseCase(appTheme)
    }

    private fun showLoadingMessage() = viewModelScope.launch {
        messageHelper.showLoading()
        delay(3000L)
        messageHelper.hideLoading()
    }

    private fun showSnackbarMessage() {
        emitEffect(
            SettingsContract.Effect.ShowSnackbar(
                message = context.getString(CryptoString.cb_settings_test_snackbar_message),
                actionLabel = context.getString(CryptoString.cb_settings_test_snackbar_action),
            ),
        )
    }
}
