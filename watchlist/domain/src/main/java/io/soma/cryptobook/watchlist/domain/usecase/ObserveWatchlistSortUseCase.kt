package io.soma.cryptobook.watchlist.domain.usecase

import io.soma.cryptobook.core.domain.model.CoinSortState
import io.soma.cryptobook.core.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveWatchlistSortUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    operator fun invoke(): Flow<CoinSortState> = userDataRepository.userData
        .map {
            CoinSortState(
                column = it.watchlistCoinSortColumn,
                direction = it.watchlistCoinSortDirection,
            )
        }
        .distinctUntilChanged()
}
