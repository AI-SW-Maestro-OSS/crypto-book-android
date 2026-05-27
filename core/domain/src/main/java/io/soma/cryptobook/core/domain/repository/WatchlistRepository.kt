package io.soma.cryptobook.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    /** Emits the symbols currently saved to the watchlist. */
    fun observeWatchlist(): Flow<List<String>>

    /** Emits whether [symbol] is currently in the watchlist. */
    fun observeIsWatchlisted(symbol: String): Flow<Boolean>

    /** Adds [symbol] if absent, removes it if present. */
    suspend fun toggle(symbol: String)
}
