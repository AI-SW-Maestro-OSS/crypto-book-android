package io.soma.cryptobook.core.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

public abstract class BaseViewModel<STATE, EVENT, EFFECT>(
    initialState: STATE,
) : ViewModel(),
    UnidirectionalViewModel<STATE, EVENT, EFFECT> {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<STATE> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<EFFECT>()
    override val effect: SharedFlow<EFFECT> = _effect.asSharedFlow()

    private val handledOneTimeEvents = mutableSetOf<EVENT>()

    protected fun updateState(update: (STATE) -> STATE) {
        _state.update(update)
    }

    protected fun emitEffect(effect: EFFECT) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }

    protected fun handleOneTimeEvent(event: EVENT, block: () -> Unit) {
        if (event !in handledOneTimeEvents) {
            handledOneTimeEvents.add(event)
            block()
        }

    }
}