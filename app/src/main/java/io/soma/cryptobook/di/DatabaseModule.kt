package io.soma.cryptobook.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.soma.cryptobook.core.data.database.CryptoBookDatabase
import io.soma.cryptobook.core.data.database.ticksize.SymbolTickSizeDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideCryptoBookDatabase(@ApplicationContext context: Context): CryptoBookDatabase =
        Room.databaseBuilder(
            context,
            CryptoBookDatabase::class.java,
            "cryptobook.db",
        ).build()

    @Provides
    @Singleton
    fun provideSymbolTickSizeDao(database: CryptoBookDatabase): SymbolTickSizeDao =
        database.symbolTickSizeDao()
}
