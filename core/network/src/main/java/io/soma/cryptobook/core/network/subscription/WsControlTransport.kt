package io.soma.cryptobook.core.network.subscription

interface WsControlTransport {
    val isSocketReady: Boolean

    fun sendCommand(method: String, params: List<Any>, id: Int): Boolean
}
