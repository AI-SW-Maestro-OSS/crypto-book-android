package io.soma.cryptobook.coindetail.domain.usecase

import io.soma.cryptobook.coindetail.domain.model.CoinDetailVO
import io.soma.cryptobook.coindetail.domain.repository.CoinDetailRepository
import io.soma.cryptobook.core.domain.error.WebSocketDisconnectedException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveCoinDetailUseCase @Inject constructor(
    private val repository: CoinDetailRepository,
) {
    sealed interface Result {
        data class Success(val coinDetail: CoinDetailVO) : Result
        sealed interface Error : Result {
            data class Connection(val throwable: Throwable) : Error
            object Disconnected : Error
        }
    }

    operator fun invoke(symbol: String): Flow<Result> = repository.observeCoinDetail(symbol)
        .map<CoinDetailVO, Result> { coinDetail ->
            Result.Success(coinDetail)
        }.catch { e ->
            when (e) {
                is WebSocketDisconnectedException -> {
                    emit(Result.Error.Disconnected)
                }

                else -> {
                    emit(Result.Error.Connection(e))
                }
            }
        }
}
