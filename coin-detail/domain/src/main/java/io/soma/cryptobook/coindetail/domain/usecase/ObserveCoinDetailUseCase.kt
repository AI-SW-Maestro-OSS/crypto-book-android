package io.soma.cryptobook.coindetail.domain.usecase

import io.soma.cryptobook.coindetail.domain.model.CoinCandleVO
import io.soma.cryptobook.coindetail.domain.model.CoinDetailStreamState
import io.soma.cryptobook.coindetail.domain.model.CoinDetailVO
import io.soma.cryptobook.coindetail.domain.model.OrderBookVO
import io.soma.cryptobook.coindetail.domain.repository.CoinDetailRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveCoinDetailUseCase @Inject constructor(private val repository: CoinDetailRepository) {
    sealed interface Result {
        data object Loading : Result
        data class Success(
            val coinDetail: CoinDetailVO,
            val candles: List<CoinCandleVO>,
            val orderBook: OrderBookVO?,
        ) : Result

        sealed interface Error : Result {
            data class Connection(val throwable: Throwable) : Error
        }
    }

    operator fun invoke(symbol: String): Flow<Result> = repository.observeCoinDetail(symbol)
        .map<CoinDetailStreamState, Result> { state ->
            when (state) {
                is CoinDetailStreamState.Loading -> Result.Loading

                is CoinDetailStreamState.Data -> Result.Success(
                    coinDetail = state.value,
                    candles = state.candles,
                    orderBook = state.orderBook,
                )
            }
        }.catch { e ->
            emit(Result.Error.Connection(e))
        }
}
