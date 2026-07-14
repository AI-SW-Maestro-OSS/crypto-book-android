package io.soma.cryptobook.main.presentation.navigation

import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavKey
import io.soma.cryptobook.navigation.NavigationState

@Stable
class CryptoNavigator(val state: NavigationState) {
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

    fun goBack() {
        state.backStack.removeLastOrNull()
    }
}
