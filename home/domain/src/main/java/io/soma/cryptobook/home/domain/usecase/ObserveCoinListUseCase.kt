package io.soma.cryptobook.home.domain.usecase

import io.soma.cryptobook.core.domain.model.CoinPriceVO
import io.soma.cryptobook.core.domain.repository.CoinRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveCoinListUseCase @Inject constructor(
    private val coinRepository: CoinRepository,
) {
    sealed class Result {
        data class Success(val coinList: List<CoinPriceVO>) : Result()
        sealed class Error : Result() {
            data class Connection(val throwable: Throwable) : Error()
        }
    }

    operator fun invoke(): Flow<Result> {
        return coinRepository.observeCoinPrices()
            .map<List<CoinPriceVO>, Result> { coins ->
                Result.Success(coins)
            }.catch { e ->
                emit(Result.Error.Connection(e))
            }
    }
}
