package io.soma.cryptobook.di

import io.soma.cryptobook.coindetail.data.network.BinanceFuturesKlineClient
import io.soma.cryptobook.core.network.base.BaseDataSource
import kotlinx.serialization.json.JsonElement
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject

interface BinanceFuturesApiService {
    @GET("fapi/v1/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int,
    ): Response<List<List<JsonElement>>>
}

class DefaultBinanceFuturesKlineClient @Inject constructor(
    private val apiService: BinanceFuturesApiService,
) : BaseDataSource(), BinanceFuturesKlineClient {
    override suspend fun getKlines(
        symbol: String,
        interval: String,
        limit: Int,
    ): List<List<JsonElement>> = checkResponse(
        apiService.getKlines(
            symbol = symbol,
            interval = interval,
            limit = limit,
        ),
    )
}
