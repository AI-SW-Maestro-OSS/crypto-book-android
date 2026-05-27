package io.soma.cryptobook.core.domain.model

data class CoinSortState(
    val column: CoinSortColumn = CoinSortColumn.NONE,
    val direction: CoinSortDirection = CoinSortDirection.NONE,
)

/**
 * Returns the next sort state when the given [column] header is tapped.
 *
 * Tapping the active column cycles ASC -> DESC -> NONE; tapping another column
 * starts at ASC on that column.
 */
fun CoinSortState.next(column: CoinSortColumn): CoinSortState = if (column == this.column) {
    when (direction) {
        CoinSortDirection.NONE -> CoinSortState(column, CoinSortDirection.ASC)
        CoinSortDirection.ASC -> CoinSortState(column, CoinSortDirection.DESC)
        CoinSortDirection.DESC -> CoinSortState(CoinSortColumn.NONE, CoinSortDirection.NONE)
    }
} else {
    CoinSortState(column, CoinSortDirection.ASC)
}
