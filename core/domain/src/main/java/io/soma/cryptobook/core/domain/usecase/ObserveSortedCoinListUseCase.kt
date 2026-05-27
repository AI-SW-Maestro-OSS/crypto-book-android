package io.soma.cryptobook.core.domain.usecase

import io.soma.cryptobook.core.domain.error.CoinPriceError
import io.soma.cryptobook.core.domain.model.CoinPriceVO
import io.soma.cryptobook.core.domain.model.CoinSortColumn
import io.soma.cryptobook.core.domain.model.CoinSortDirection
import io.soma.cryptobook.core.domain.model.CoinSortState
import io.soma.cryptobook.core.domain.outcome.Outcome
import io.soma.cryptobook.core.domain.outcome.mapSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Applies sorting to a live-updating coin price stream while keeping row order stable.
 *
 * The sort order is frozen the moment sorting changes (or on the first emission of a
 * collection) so that subsequent live price updates do not reshuffle rows. New symbols
 * are appended at the end. When no sort is active the prices are emitted unchanged.
 *
 * The freeze state lives within the collection scope, so its lifetime matches a single
 * observe session — both Home and Watchlist reuse this by supplying their own
 * [prices] and [sort] flows.
 */
class ObserveSortedCoinListUseCase @Inject constructor() {
    operator fun invoke(
        prices: Flow<Outcome<List<CoinPriceVO>, CoinPriceError>>,
        sort: Flow<CoinSortState>,
    ): Flow<Outcome<List<CoinPriceVO>, CoinPriceError>> = flow {
        var frozenOrder: List<String>? = null
        var lastSort: CoinSortState? = null

        combine(prices, sort) { pricesOutcome, sortState -> pricesOutcome to sortState }
            .collect { (pricesOutcome, sortState) ->
                if (sortState != lastSort) {
                    frozenOrder = null
                    lastSort = sortState
                }

                val ordered = pricesOutcome.mapSuccess { coins ->
                    if (sortState.column == CoinSortColumn.NONE ||
                        sortState.direction == CoinSortDirection.NONE
                    ) {
                        frozenOrder = null
                        coins
                    } else {
                        val order = frozenOrder
                            ?: computeSortedSymbols(coins, sortState).also { frozenOrder = it }
                        val bySymbol = coins.associateBy { it.symbol }
                        val known = order.toHashSet()
                        order.mapNotNull { bySymbol[it] } + coins.filter { it.symbol !in known }
                    }
                }

                emit(ordered)
            }
    }

    private fun computeSortedSymbols(coins: List<CoinPriceVO>, sort: CoinSortState): List<String> {
        val base: Comparator<CoinPriceVO> = when (sort.column) {
            CoinSortColumn.SYMBOL -> compareBy { it.symbol }
            CoinSortColumn.PRICE -> compareBy { it.price }
            CoinSortColumn.CHANGE -> compareBy { it.priceChangePercentage24h }
            CoinSortColumn.VOLUME -> compareBy { it.quoteVolume }
            CoinSortColumn.NONE -> return coins.map { it.symbol }
        }
        val directed = if (sort.direction == CoinSortDirection.DESC) base.reversed() else base
        return coins.sortedWith(directed.thenBy { it.symbol }).map { it.symbol }
    }
}
