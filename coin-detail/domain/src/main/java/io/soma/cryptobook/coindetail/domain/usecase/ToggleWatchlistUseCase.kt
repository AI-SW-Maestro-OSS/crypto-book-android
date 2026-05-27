package io.soma.cryptobook.coindetail.domain.usecase

import io.soma.cryptobook.core.domain.repository.WatchlistRepository
import javax.inject.Inject

class ToggleWatchlistUseCase @Inject constructor(
    private val watchlistRepository: WatchlistRepository,
) {
    suspend operator fun invoke(symbol: String) {
        watchlistRepository.toggle(symbol)
    }
}
