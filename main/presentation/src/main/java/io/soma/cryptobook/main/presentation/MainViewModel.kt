package io.soma.cryptobook.main.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.core.domain.model.Language
import io.soma.cryptobook.core.domain.usecase.GetUserDataUseCase
import io.soma.cryptobook.core.domain.usecase.SetLanguageUseCase
import io.soma.cryptobook.core.presentation.mvi.BaseViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
) : BaseViewModel<MainContract.State, MainContract.Event, MainContract.Effect>(
    initialState = MainContract.State,
),
    MainContract.ViewModel {
    init {
        observeLanguage()
    }

    override fun event(event: MainContract.Event) {
        when (event) {
            is MainContract.Event.OnSystemLocaleDetected -> persistSystemLocale(event.language)
        }
    }

    private fun observeLanguage() {
        getUserDataUseCase()
            .map { it.language }
            .distinctUntilChanged()
            .onEach { language ->
                emitEffect(MainContract.Effect.ApplyLocale(localeTag = language.localeTag))
            }
            .launchIn(viewModelScope)
    }

    private fun persistSystemLocale(language: Language) {
        viewModelScope.launch {
            setLanguageUseCase(language)
        }
    }
}
