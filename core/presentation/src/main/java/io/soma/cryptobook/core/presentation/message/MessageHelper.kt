package io.soma.cryptobook.core.presentation.message

import io.soma.cryptobook.core.designsystem.util.Text

interface MessageHelper {
    fun showLoading()
    fun hideLoading()
    fun showToast(message: String)
    fun showToast(text: Text)
}
