package io.soma.cryptobook.main.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.core.domain.model.AppTheme
import io.soma.cryptobook.core.domain.model.Language
import io.soma.cryptobook.core.domain.usecase.GetUserDataUseCase
import io.soma.cryptobook.core.domain.usecase.SetLanguageUseCase
import io.soma.cryptobook.core.ui.BackgroundEvent
import io.soma.cryptobook.core.ui.BaseViewModel
import io.soma.cryptobook.main.presentation.util.toNightMode
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for [MainActivity]. Owns the app-wide locale and theme, which are applied to the
 * Activity rather than rendered, and so are delivered as events.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    getUserDataUseCase: GetUserDataUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
) : BaseViewModel<MainState, MainEvent, MainAction>(
    initialState = MainState,
) {

    init {
        getUserDataUseCase()
            .map { it.language }
            .distinctUntilChanged()
            .map { MainAction.Internal.ReceiveLanguage(language = it) }
            .onEach(::sendAction)
            .launchIn(viewModelScope)

        getUserDataUseCase()
            .map { it.appTheme }
            .distinctUntilChanged()
            .map { MainAction.Internal.ReceiveAppTheme(appTheme = it) }
            .onEach(::sendAction)
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: MainAction) {
        when (action) {
            is MainAction.SystemLocaleDetected -> handleSystemLocaleDetected(action)
            is MainAction.Internal.ReceiveLanguage -> handleReceiveLanguage(action)
            is MainAction.Internal.ReceiveAppTheme -> handleReceiveAppTheme(action)
        }
    }

    private fun handleSystemLocaleDetected(action: MainAction.SystemLocaleDetected) {
        viewModelScope.launch { setLanguageUseCase(action.language) }
    }

    private fun handleReceiveLanguage(action: MainAction.Internal.ReceiveLanguage) {
        val language = action.language
        val localeTag = if (language == Language.SYSTEM) null else language.localeTag
        sendEvent(MainEvent.ApplyLocale(localeTag = localeTag))
    }

    private fun handleReceiveAppTheme(action: MainAction.Internal.ReceiveAppTheme) {
        sendEvent(MainEvent.ApplyTheme(nightMode = action.appTheme.toNightMode()))
    }
}

/**
 * State for [MainActivity]. The Activity renders nothing of its own.
 */
data object MainState

/**
 * One-shot events for [MainActivity].
 *
 * These are [BackgroundEvent]s: the locale and theme are emitted as soon as user data is first
 * read, which happens while the Activity is still starting up. A lifecycle-gated event would be
 * dropped before the Activity reaches RESUMED, leaving the app on the wrong theme and locale.
 */
sealed class MainEvent : BackgroundEvent {
    /**
     * A [localeTag] of `null` means "follow the system language" (an empty locale list).
     */
    data class ApplyLocale(val localeTag: String?) : MainEvent()

    data class ApplyTheme(val nightMode: Int) : MainEvent()
}

/**
 * User and system actions for [MainActivity].
 */
sealed class MainAction {
    data class SystemLocaleDetected(val language: Language) : MainAction()

    /**
     * Internal actions dispatched by the ViewModel from coroutines.
     */
    sealed class Internal : MainAction() {
        data class ReceiveLanguage(val language: Language) : Internal()

        data class ReceiveAppTheme(val appTheme: AppTheme) : Internal()
    }
}
