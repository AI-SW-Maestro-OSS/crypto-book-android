package io.soma.cryptobook.core.data.database.ticksize

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symbol_tick_sizes")
data class SymbolTickSizeEntity(
    @PrimaryKey val symbol: String,
    val tickSize: String,
)
