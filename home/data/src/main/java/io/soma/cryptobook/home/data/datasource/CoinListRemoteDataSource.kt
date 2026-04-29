package io.soma.cryptobook.home.data.datasource

import io.soma.cryptobook.core.domain.outcome.Outcome
import io.soma.cryptobook.core.network.base.BaseDataSource
import io.soma.cryptobook.core.network.error.ApiError
import io.soma.cryptobook.home.data.model.BinanceTickerDto
import io.soma.cryptobook.home.data.network.BinanceApiService
import javax.inject.Inject

class CoinListRemoteDataSource @Inject constructor(
    private val apiService: BinanceApiService,
) : BaseDataSource() {
    suspend fun getAllTickerPrices(): Outcome<List<BinanceTickerDto>, ApiError> = executeApiCall {
        apiService.getAllTickerPrices()
    }
}
