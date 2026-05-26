package io.soma.cryptobook.home.domain.usecase

import io.soma.cryptobook.core.domain.repository.UserDataRepository
import io.soma.cryptobook.home.domain.model.CoinSortState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveCoinSortUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    operator fun invoke(): Flow<CoinSortState> = userDataRepository.userData
        .map { CoinSortState(column = it.coinSortColumn, direction = it.coinSortDirection) }
        .distinctUntilChanged()
}
