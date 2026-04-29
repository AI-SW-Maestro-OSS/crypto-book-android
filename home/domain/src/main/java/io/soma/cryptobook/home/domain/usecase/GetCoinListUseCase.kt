package io.soma.cryptobook.home.domain.usecase

import io.soma.cryptobook.core.domain.error.CoinPriceError
import io.soma.cryptobook.core.domain.model.CoinPriceVO
import io.soma.cryptobook.core.domain.outcome.Outcome
import io.soma.cryptobook.core.domain.repository.CoinRepository
import javax.inject.Inject

class GetCoinListUseCase @Inject constructor(
    private val coinRepository: CoinRepository,
) {
    suspend operator fun invoke(): Outcome<List<CoinPriceVO>, CoinPriceError> {
        return coinRepository.getCoinPrices()
    }
}
