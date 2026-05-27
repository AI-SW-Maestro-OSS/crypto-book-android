package io.soma.cryptobook.watchlist.domain.usecase

import io.soma.cryptobook.core.domain.error.CoinPriceError
import io.soma.cryptobook.core.domain.model.CoinPriceVO
import io.soma.cryptobook.core.domain.outcome.Outcome
import io.soma.cryptobook.core.domain.outcome.mapSuccess
import io.soma.cryptobook.core.domain.repository.CoinRepository
import io.soma.cryptobook.core.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Emits the live coin price stream filtered down to the symbols saved in the watchlist.
 *
 * Reuses [CoinRepository.observeCoinPrices], so prices update in realtime exactly as on
 * the Home screen; only the set of rows differs.
 */
class ObserveWatchlistUseCase @Inject constructor(
    private val coinRepository: CoinRepository,
    private val watchlistRepository: WatchlistRepository,
) {
    operator fun invoke(): Flow<Outcome<List<CoinPriceVO>, CoinPriceError>> = combine(
        coinRepository.observeCoinPrices(),
        watchlistRepository.observeWatchlist(),
    ) { pricesOutcome, watchlistSymbols ->
        val symbols = watchlistSymbols.toHashSet()
        pricesOutcome.mapSuccess { prices ->
            prices.filter { it.symbol in symbols }
        }
    }
}
