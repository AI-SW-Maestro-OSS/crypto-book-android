package io.soma.cryptobook.core.network.market

import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject

class WsMarketMessageParser @Inject constructor(
    private val json: Json,
) {
    companion object {
        private const val TAG = "WsMarketMessageParser"
        private const val EVENT_24HR_TICKER = "24hrTicker"
    }

    fun parse(raw: String): WsMarketMessage {
        val message = raw.trim()
        if (message.isEmpty()) return WsMarketMessage.Ignored
        if (isControlAckMessage(message)) return WsMarketMessage.Ignored

        return when {
            message.startsWith("[") -> parseAllTickers(message)
            message.startsWith("{") -> parseSymbolTicker(message)
            else -> WsMarketMessage.Ignored
        }
    }

    private fun isControlAckMessage(message: String): Boolean {
        val jsonObject = runCatching {
            json.parseToJsonElement(message).jsonObject
        }.getOrNull() ?: return false

        val hasId = jsonObject.containsKey("id")
        if (!hasId) return false
        return jsonObject.containsKey("result") ||
            jsonObject.containsKey("code") ||
            jsonObject.containsKey("msg")
    }

    private fun parseAllTickers(message: String): WsMarketMessage = runCatching {
        val tickers = json.decodeFromString<List<WsTickerPayload>>(message)
        val payload = tickers.filter { it.eventType == EVENT_24HR_TICKER }
        if (payload.isEmpty()) {
            WsMarketMessage.Ignored
        } else {
            WsMarketMessage.AllTickers(payload)
        }
    }.getOrElse { throwable ->
        Log.d(TAG, "Ignored market array message: ${throwable.message}")
        WsMarketMessage.Ignored
    }

    private fun parseSymbolTicker(message: String): WsMarketMessage = runCatching {
        val ticker = json.decodeFromString<WsTickerPayload>(message)
        if (ticker.eventType == EVENT_24HR_TICKER) {
            WsMarketMessage.SymbolTicker(ticker)
        } else {
            WsMarketMessage.Ignored
        }
    }.getOrElse { throwable ->
        Log.d(TAG, "Ignored market object message: ${throwable.message}")
        WsMarketMessage.Ignored
    }
}
