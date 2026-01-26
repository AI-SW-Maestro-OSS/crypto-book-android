package io.soma.cryptobook.core.data.image

import io.soma.cryptobook.core.domain.image.CoinImageResolver
import javax.inject.Inject

class CoinImageResolverImpl @Inject constructor() : CoinImageResolver {

    override fun getImageUrl(symbol: String): String {
        val baseSymbol = extractBaseSymbol(symbol).lowercase()
        return "$BASE_URL$baseSymbol.svg"
    }

    private fun extractBaseSymbol(symbol: String): String {
        QUOTE_CURRENCIES.forEach { quote ->
            if (symbol.endsWith(quote)) {
                return symbol.removeSuffix(quote)
            }
        }
        return symbol
    }

    companion object {
        private const val BASE_URL =
            "https://raw.githubusercontent.com/Cryptofonts/cryptoicons/master/SVG/"
        private val QUOTE_CURRENCIES = listOf("USDT", "USDC", "BUSD", "BTC", "ETH")
    }
}
