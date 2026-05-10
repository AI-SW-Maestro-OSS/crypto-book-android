package io.soma.cryptobook.search.domain.usecase

import io.soma.cryptobook.core.domain.error.CoinPriceError
import io.soma.cryptobook.core.domain.outcome.Outcome
import io.soma.cryptobook.core.domain.outcome.mapSuccess
import io.soma.cryptobook.core.domain.repository.CoinRepository
import io.soma.cryptobook.search.domain.model.SearchCoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveSearchCoinsUseCase @Inject constructor(
    private val coinRepository: CoinRepository,
) {
    operator fun invoke(): Flow<Outcome<List<SearchCoin>, CoinPriceError>> {
        return coinRepository.observeCoinPrices()
            .map { outcome ->
                outcome.mapSuccess { coins ->
                    coins
                        .map { SearchCoin(symbol = it.symbol) }
                        .distinctBy { it.symbol }
                        .sortedBy { it.symbol }
                }
            }
    }
}
