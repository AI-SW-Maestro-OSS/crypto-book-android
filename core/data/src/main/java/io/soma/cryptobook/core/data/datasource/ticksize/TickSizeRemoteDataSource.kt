package io.soma.cryptobook.core.data.datasource.ticksize

import io.soma.cryptobook.core.data.model.ticksize.BinanceExchangeInfoDto
import io.soma.cryptobook.core.network.base.BaseDataSource
import javax.inject.Inject

class TickSizeRemoteDataSource @Inject constructor(
    private val apiService: BinanceExchangeInfoApiService,
) : BaseDataSource() {
    suspend fun getExchangeInfo(): BinanceExchangeInfoDto = checkResponse(
        apiService.getExchangeInfo(),
    )
}
