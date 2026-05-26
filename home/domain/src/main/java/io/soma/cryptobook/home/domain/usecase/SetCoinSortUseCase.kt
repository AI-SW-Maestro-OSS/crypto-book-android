package io.soma.cryptobook.home.domain.usecase

import io.soma.cryptobook.core.domain.model.CoinSortColumn
import io.soma.cryptobook.core.domain.model.CoinSortDirection
import io.soma.cryptobook.core.domain.repository.UserDataRepository
import javax.inject.Inject

class SetCoinSortUseCase @Inject constructor(private val userDataRepository: UserDataRepository) {
    suspend operator fun invoke(column: CoinSortColumn, direction: CoinSortDirection) {
        userDataRepository.setCoinSort(column, direction)
    }
}
