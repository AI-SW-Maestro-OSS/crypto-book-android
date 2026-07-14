package io.soma.cryptobook.diary.presentation

import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.core.ui.BaseViewModel
import javax.inject.Inject

/**
 * ViewModel for the Diary screen, which is still a placeholder.
 */
@HiltViewModel
class DiaryViewModel @Inject constructor() :
    BaseViewModel<DiaryState, DiaryEvent, DiaryAction>(
        initialState = DiaryState(),
    ) {
    override fun handleAction(action: DiaryAction) = Unit
}

/**
 * State for the Diary screen.
 */
data class DiaryState(val isLoading: Boolean = false)

/**
 * One-shot UI events for the Diary screen. The screen currently has none.
 */
sealed class DiaryEvent

/**
 * User and system actions for the Diary screen. The screen currently has none.
 */
sealed class DiaryAction
