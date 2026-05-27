package io.soma.cryptobook.watchlist.domain.usecase

import io.soma.cryptobook.core.domain.model.CoinSortColumn
import io.soma.cryptobook.core.domain.model.CoinSortDirection
import io.soma.cryptobook.core.domain.repository.UserDataRepository
import javax.inject.Inject

class SetWatchlistSortUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(column: CoinSortColumn, direction: CoinSortDirection) {
        userDataRepository.setWatchlistCoinSort(column, direction)
    }
}
