package io.soma.cryptobook.core.data.datasource.ticksize

import io.soma.cryptobook.core.data.model.ticksize.BinanceExchangeInfoDto
import retrofit2.Response
import retrofit2.http.GET

interface BinanceExchangeInfoApiService {
    @GET("api/v3/exchangeInfo")
    suspend fun getExchangeInfo(): Response<BinanceExchangeInfoDto>
}
