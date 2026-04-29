package io.soma.cryptobook.core.domain.repository

import io.soma.cryptobook.core.domain.error.CoinPriceError
import io.soma.cryptobook.core.domain.model.CoinInfoVO
import io.soma.cryptobook.core.domain.model.CoinPriceVO
import io.soma.cryptobook.core.domain.outcome.Outcome
import kotlinx.coroutines.flow.Flow

interface CoinRepository {
    suspend fun getCoinPrices(): Outcome<List<CoinPriceVO>, CoinPriceError>
    suspend fun getCoinInfoList(): List<CoinInfoVO>

    // 웹소켓용은 추후 구현
    fun observeCoinPrices(): Flow<Outcome<List<CoinPriceVO>, CoinPriceError>>
}
