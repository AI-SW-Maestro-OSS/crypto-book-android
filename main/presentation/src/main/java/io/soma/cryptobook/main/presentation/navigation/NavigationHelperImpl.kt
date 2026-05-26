package io.soma.cryptobook.main.presentation.navigation

import io.soma.cryptobook.core.domain.navigation.AppPage
import io.soma.cryptobook.core.domain.navigation.NavigationHelper
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationHelperImpl @Inject constructor() :
    NavigationHelper,
    NavCommandSource {
    private val _commands = MutableSharedFlow<NavCommand>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val commands: SharedFlow<NavCommand> = _commands.asSharedFlow()

    override fun navigate(page: AppPage) {
        _commands.tryEmit(NavCommand.Navigate(page))
    }

    override fun deepLink(link: String) {
        _commands.tryEmit(NavCommand.DeepLink(link))
    }

    override fun back() {
        _commands.tryEmit(NavCommand.Back)
    }
}
