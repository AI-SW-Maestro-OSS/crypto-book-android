package io.soma.cryptobook.core.data.database.watchlist

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(@PrimaryKey val symbol: String, val addedAt: Long)
