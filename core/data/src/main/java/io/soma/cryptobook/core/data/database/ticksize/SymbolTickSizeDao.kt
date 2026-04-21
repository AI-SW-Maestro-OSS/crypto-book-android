package io.soma.cryptobook.core.data.database.ticksize

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SymbolTickSizeDao {
    @Query("SELECT * FROM symbol_tick_sizes")
    fun observeAll(): Flow<List<SymbolTickSizeEntity>>

    @Query("SELECT tickSize FROM symbol_tick_sizes WHERE symbol = :symbol")
    fun observeTickSize(symbol: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tickSizes: List<SymbolTickSizeEntity>)

    @Query("DELETE FROM symbol_tick_sizes")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(tickSizes: List<SymbolTickSizeEntity>) {
        clear()
        upsertAll(tickSizes)
    }
}
