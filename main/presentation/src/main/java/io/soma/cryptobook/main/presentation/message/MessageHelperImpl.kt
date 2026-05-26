package io.soma.cryptobook.main.presentation.message

import io.soma.cryptobook.core.domain.message.MessageHelper
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageHelperImpl @Inject constructor() :
    MessageHelper,
    MessageCommandSource {
    private val loadingCount = atomic(0)

    private val _commands = MutableSharedFlow<MessageCommand>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val commands: SharedFlow<MessageCommand> = _commands.asSharedFlow()

    override fun showLoading() {
        if (loadingCount.incrementAndGet() == 1) {
            _commands.tryEmit(MessageCommand.ShowLoading)
        }
    }

    override fun hideLoading() {
        if (loadingCount.decrementAndGet() == 0) {
            _commands.tryEmit(MessageCommand.HideLoading)
        }
    }

    override fun showToast(message: String) {
        _commands.tryEmit(MessageCommand.ShowToast(message))
    }
}
