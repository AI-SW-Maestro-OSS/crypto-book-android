package io.soma.cryptobook.main.presentation.message

import io.soma.cryptobook.core.designsystem.util.Text
import kotlinx.coroutines.flow.SharedFlow

sealed interface MessageCommand {
    data object ShowLoading : MessageCommand
    data object HideLoading : MessageCommand
    data class ShowToast(val text: Text) : MessageCommand
}

interface MessageCommandSource {
    val commands: SharedFlow<MessageCommand>
}
