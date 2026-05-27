package io.soma.cryptobook.diary.presentation

import dagger.hilt.android.lifecycle.HiltViewModel
import io.soma.cryptobook.core.presentation.mvi.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor() :
    BaseViewModel<DiaryContract.State, DiaryContract.Event, DiaryContract.Effect>(
        DiaryContract.State(),
    ),
    DiaryContract.ViewModel {
    override fun event(event: DiaryContract.Event) {
    }
}
