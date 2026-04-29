package io.soma.cryptobook.home.domain.usecase

import io.soma.cryptobook.core.domain.error.CoinPriceError
import io.soma.cryptobook.core.domain.model.CoinPriceVO
import io.soma.cryptobook.core.domain.outcome.Outcome
import io.soma.cryptobook.core.domain.repository.CoinRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCoinListUseCase @Inject constructor(
    private val coinRepository: CoinRepository,
) {
    operator fun invoke(): Flow<Outcome<List<CoinPriceVO>, CoinPriceError>> {
        return coinRepository.observeCoinPrices()
    }
}
