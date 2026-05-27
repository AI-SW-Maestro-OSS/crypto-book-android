package io.soma.cryptobook.core.data.repository

import io.soma.cryptobook.core.data.database.watchlist.WatchlistDao
import io.soma.cryptobook.core.data.database.watchlist.WatchlistEntity
import io.soma.cryptobook.core.domain.repository.WatchlistRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WatchlistRepositoryImpl @Inject constructor(
    private val watchlistDao: WatchlistDao,
    private val ioDispatcher: CoroutineDispatcher,
) : WatchlistRepository {
    override fun observeWatchlist(): Flow<List<String>> = watchlistDao.observeSymbols()

    override fun observeIsWatchlisted(symbol: String): Flow<Boolean> =
        watchlistDao.observeIsWatchlisted(symbol)

    override suspend fun toggle(symbol: String) = withContext(ioDispatcher) {
        if (watchlistDao.isWatchlisted(symbol)) {
            watchlistDao.delete(symbol)
        } else {
            watchlistDao.insert(
                WatchlistEntity(symbol = symbol, addedAt = System.currentTimeMillis()),
            )
        }
    }
}
