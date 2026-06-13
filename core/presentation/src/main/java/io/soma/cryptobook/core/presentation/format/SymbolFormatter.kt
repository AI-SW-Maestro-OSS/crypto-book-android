package io.soma.cryptobook.core.presentation.format

/**
 * Splits a combined market symbol (e.g. "BTCUSDT") into its base and quote
 * assets (e.g. "BTC" and "USDT").
 *
 * When no known quote currency suffix matches, the whole symbol is treated as
 * the base and the quote is returned empty.
 */
object SymbolFormatter {
    private val QUOTE_CURRENCIES = listOf("USDT", "USDC", "BUSD", "BTC", "ETH")

    fun split(symbol: String): SymbolParts {
        QUOTE_CURRENCIES.forEach { quote ->
            if (symbol.length > quote.length && symbol.endsWith(quote)) {
                return SymbolParts(base = symbol.removeSuffix(quote), quote = quote)
            }
        }
        return SymbolParts(base = symbol, quote = "")
    }
}

data class SymbolParts(
    val base: String,
    val quote: String,
)
