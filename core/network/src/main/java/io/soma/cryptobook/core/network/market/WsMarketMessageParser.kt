package io.soma.cryptobook.core.network.market

import android.util.Log
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class WsMarketMessageParser @Inject constructor(
    private val json: Json,
) {
    companion object {
        private const val TAG = "WsMarketMessageParser"
        private const val EVENT_24HR_TICKER = "24hrTicker"
        private const val EVENT_24HR_MINI_TICKER = "24hrMiniTicker"
        private const val EVENT_KLINE = "kline"
    }

    fun parse(raw: String): WsMarketMessage {
        val message = raw.trim()
        if (message.isEmpty()) return WsMarketMessage.Ignored
        if (isControlAckMessage(message)) return WsMarketMessage.Ignored

        return when {
            message.startsWith("[") -> parseArrayMessage(message)
            message.startsWith("{") -> parseSymbolMessage(message)
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

    private fun parseArrayMessage(message: String): WsMarketMessage = runCatching {
        val element = json.parseToJsonElement(message)
        val payload = element.jsonArray
        val eventType = payload.firstOrNull()
            ?.jsonObject
            ?.get("e")
            ?.jsonPrimitive
            ?.contentOrNull

        when (eventType) {
            EVENT_24HR_MINI_TICKER -> {
                val tickers = json.decodeFromJsonElement<List<WsMiniTickerPayload>>(element)
                WsMarketMessage.AllMiniTickers(tickers)
            }

            else -> {
                WsMarketMessage.Ignored
            }
        }
    }.getOrElse { throwable ->
        Log.d(TAG, "[WS_PARSE] action=ignored kind=array reason=${throwable.message}")
        WsMarketMessage.Ignored
    }

    private fun parseSymbolMessage(message: String): WsMarketMessage = runCatching {
        val element = json.parseToJsonElement(message)
        val jsonObject = element.jsonObject
        when (jsonObject["e"]?.jsonPrimitive?.contentOrNull) {
            EVENT_24HR_TICKER -> {
                val ticker = json.decodeFromJsonElement<WsTickerPayload>(element)
                WsMarketMessage.SymbolTicker(ticker)
            }

            EVENT_KLINE -> {
                val klineEvent = json.decodeFromJsonElement<WsKlineEventPayload>(element)
                WsMarketMessage.SymbolKline(klineEvent)
            }

            else -> WsMarketMessage.Ignored
        }
    }.getOrElse { throwable ->
        Log.d(TAG, "[WS_PARSE] action=ignored kind=object reason=${throwable.message}")
        WsMarketMessage.Ignored
    }
}
