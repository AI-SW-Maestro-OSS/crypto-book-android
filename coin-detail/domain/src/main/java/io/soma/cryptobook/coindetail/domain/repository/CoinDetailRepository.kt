package io.soma.cryptobook.coindetail.domain.repository

import io.soma.cryptobook.coindetail.domain.model.CoinDetailVO
import kotlinx.coroutines.flow.Flow

interface CoinDetailRepository {
    fun observeCoinDetail(symbol: String): Flow<CoinDetailVO>
}
