package io.soma.cryptobook.core.domain.message

interface MessageHelper {
    fun showLoading()
    fun hideLoading()
    fun showToast(message: String)
}
