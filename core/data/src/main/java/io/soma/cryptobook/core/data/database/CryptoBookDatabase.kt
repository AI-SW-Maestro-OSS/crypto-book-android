package io.soma.cryptobook.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import io.soma.cryptobook.core.data.database.ticksize.SymbolTickSizeDao
import io.soma.cryptobook.core.data.database.ticksize.SymbolTickSizeEntity

@Database(
    entities = [
        SymbolTickSizeEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class CryptoBookDatabase : RoomDatabase() {
    abstract fun symbolTickSizeDao(): SymbolTickSizeDao
}
