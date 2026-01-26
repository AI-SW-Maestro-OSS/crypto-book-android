package io.soma.cryptobook.core.domain.image

interface CoinImageResolver {
    fun getImageUrl(symbol: String): String
}
