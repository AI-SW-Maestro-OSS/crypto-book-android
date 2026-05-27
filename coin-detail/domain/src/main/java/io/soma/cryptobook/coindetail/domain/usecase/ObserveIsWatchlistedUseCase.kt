package io.soma.cryptobook.coindetail.domain.usecase

import io.soma.cryptobook.core.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveIsWatchlistedUseCase @Inject constructor(
    private val watchlistRepository: WatchlistRepository,
) {
    operator fun invoke(symbol: String): Flow<Boolean> =
        watchlistRepository.observeIsWatchlisted(symbol)
}
