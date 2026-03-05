package io.soma.cryptobook.core.network.market

import android.util.Log
import java.util.concurrent.atomic.AtomicLong

internal class WsMarketDebugLogger(
    private val firstFullLogCount: Long = 20L,
    private val sampledEvery: Long = 50L,
    private val rawHeadLimit: Int = 180,
) {
    companion object {
        private const val TAG = "WsMarketDebug"
        private const val ALL_TICKERS_STREAM = "!ticker@arr"
    }

    private val sequence = AtomicLong(0L)

    fun nextSequence(): Long = sequence.incrementAndGet()

    fun logRaw(seq: Long, raw: String) {
        if (!shouldLog(seq)) return
        val trimmed = raw.trim().replace('\n', ' ').replace('\r', ' ')
        val head = if (trimmed.length <= rawHeadLimit) {
            trimmed
        } else {
            "${trimmed.take(rawHeadLimit)}..."
        }

        Log.d(
            TAG,
            "[WS_RAW] seq=$seq len=${raw.length} head=$head",
        )
    }

    fun logParsed(seq: Long, message: WsMarketMessage) {
        if (!shouldLog(seq)) return

        val type = when (message) {
            is WsMarketMessage.AllTickers -> "AllTickers"
            is WsMarketMessage.SymbolTicker -> "SymbolTicker"
            is WsMarketMessage.SymbolKline -> "SymbolKline"
            is WsMarketMessage.Ignored -> "Ignored"
        }

        Log.d(
            TAG,
            "[WS_PARSED] seq=$seq type=$type stream=${streamOf(message)} summary=${summaryOf(message)}",
        )
    }

    private fun shouldLog(seq: Long): Boolean {
        if (seq <= firstFullLogCount) return true
        if (sampledEvery <= 0L) return false
        return (seq - firstFullLogCount) % sampledEvery == 0L
    }

    private fun streamOf(message: WsMarketMessage): String = when (message) {
        is WsMarketMessage.AllTickers -> ALL_TICKERS_STREAM
        is WsMarketMessage.SymbolTicker -> "${message.ticker.symbol.lowercase()}@ticker"
        is WsMarketMessage.SymbolKline -> {
            val symbol = message.klineEvent.symbol.lowercase()
            val interval = message.klineEvent.kline.interval.lowercase()
            "${symbol}@kline_$interval"
        }

        is WsMarketMessage.Ignored -> "-"
    }

    private fun summaryOf(message: WsMarketMessage): String = when (message) {
        is WsMarketMessage.AllTickers -> {
            val topSymbols = message.tickers
                .take(3)
                .joinToString(separator = ",") { it.symbol }
                .ifEmpty { "-" }
            "count=${message.tickers.size} top3=$topSymbols"
        }

        is WsMarketMessage.SymbolTicker -> {
            val ticker = message.ticker
            "symbol=${ticker.symbol} lastPrice=${ticker.lastPrice} changePercent=${ticker.priceChangePercent}"
        }

        is WsMarketMessage.SymbolKline -> {
            val event = message.klineEvent
            val kline = event.kline
            "symbol=${event.symbol} interval=${kline.interval} openTime=${kline.openTime} closeTime=${kline.closeTime} isClosed=${kline.isClosed}"
        }

        is WsMarketMessage.Ignored -> "ignored"
    }
}
