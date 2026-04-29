package io.soma.cryptobook.core.domain.error

sealed interface CoinPriceError {
    data object Network : CoinPriceError
    data object RateLimited : CoinPriceError
    data object Server : CoinPriceError
    data object UnexpectedResponse : CoinPriceError
    data class Unknown(val message: String?) : CoinPriceError
}
