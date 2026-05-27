package io.soma.cryptobook.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import io.soma.cryptobook.core.data.database.ticksize.SymbolTickSizeDao
import io.soma.cryptobook.core.data.database.ticksize.SymbolTickSizeEntity
import io.soma.cryptobook.core.data.database.watchlist.WatchlistDao
import io.soma.cryptobook.core.data.database.watchlist.WatchlistEntity

@Database(
    entities = [
        SymbolTickSizeEntity::class,
        WatchlistEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class CryptoBookDatabase : RoomDatabase() {
    abstract fun symbolTickSizeDao(): SymbolTickSizeDao
    abstract fun watchlistDao(): WatchlistDao
}
