package io.soma.cryptobook.di

import io.soma.cryptobook.coindetail.data.model.BinanceSpotTickerDto
import io.soma.cryptobook.coindetail.data.network.BinanceSpotKlineClient
import io.soma.cryptobook.coindetail.data.network.BinanceSpotTickerClient
import io.soma.cryptobook.core.network.base.BaseDataSource
import kotlinx.serialization.json.JsonElement
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject

interface BinanceSpotApiService {
    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("startTime") startTime: Long?,
        @Query("endTime") endTime: Long?,
        @Query("limit") limit: Int,
    ): Response<List<List<JsonElement>>>

    @GET("api/v3/ticker/24hr")
    suspend fun getTicker(@Query("symbol") symbol: String): Response<BinanceSpotTickerDto>
}

class DefaultBinanceSpotKlineClient @Inject constructor(
    private val apiService: BinanceSpotApiService,
) : BaseDataSource(), BinanceSpotKlineClient {
    override suspend fun getKlines(
        symbol: String,
        interval: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
    ): List<List<JsonElement>> = checkResponse(
        apiService.getKlines(
            symbol = symbol,
            interval = interval,
            startTime = startTime,
            endTime = endTime,
            limit = limit,
        ),
    )
}

class DefaultBinanceSpotTickerClient @Inject constructor(
    private val apiService: BinanceSpotApiService,
) : BaseDataSource(), BinanceSpotTickerClient {
    override suspend fun getTicker(symbol: String): BinanceSpotTickerDto = checkResponse(
        apiService.getTicker(symbol = symbol),
    )
}
