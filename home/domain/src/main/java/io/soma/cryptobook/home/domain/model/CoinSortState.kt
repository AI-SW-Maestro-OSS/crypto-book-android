package io.soma.cryptobook.home.domain.model

import io.soma.cryptobook.core.domain.model.CoinSortColumn
import io.soma.cryptobook.core.domain.model.CoinSortDirection

data class CoinSortState(
    val column: CoinSortColumn = CoinSortColumn.NONE,
    val direction: CoinSortDirection = CoinSortDirection.NONE,
)
