package io.soma.cryptobook.main.presentation.navigation

import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavKey
import io.soma.cryptobook.navigation.NavigationState

@Stable
class CbNavigator(val state: NavigationState) {
    fun navigateTo(key: NavKey) {
        when (key) {
            in state.topLevelKeys -> goToTopLevel(key)

            else -> {
                state.backStack.remove(key)
                state.backStack.add(key)
            }
        }
    }

    fun navigateAsRoot(key: NavKey) {
        state.backStack.clear()
        state.backStack.add(state.startKey)
        state.backStack.add(key)
    }

    fun popWhileAndPush(key: NavKey, predicate: (NavKey) -> Boolean) {
        while (state.backStack.isNotEmpty() && predicate(state.backStack.last())) {
            state.backStack.removeLastOrNull()
        }
        state.backStack.add(key)
    }

    private fun goToTopLevel(key: NavKey) {
        state.backStack.apply {
            if (key == state.startKey) {
                clear()
            } else {
                remove(key)
            }
            add(key)
        }
    }

    /**
     * 딥링크에서 기존 화면을 replace하지 않고 바로 위에 추가하는 경우를 위한 함수
     */
    fun add(key: NavKey) {
        state.backStack.add(key)
    }

    fun goBack() {
        state.backStack.removeLastOrNull()
    }
}
