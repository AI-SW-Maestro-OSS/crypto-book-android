package io.soma.cryptobook.home.data.network

import io.soma.cryptobook.home.data.model.BinanceTickerDto
import retrofit2.Response
import retrofit2.http.GET

interface BinanceApiService {
    @GET("api/v3/ticker/24hr")
    suspend fun getAllTickerPrices(): Response<List<BinanceTickerDto>>
}
