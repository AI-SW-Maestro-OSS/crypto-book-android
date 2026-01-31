package io.soma.cryptobook.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.soma.cryptobook.core.network.BinanceWebSocketClient
import io.soma.cryptobook.core.network.SubscriptionTable
import io.soma.cryptobook.home.data.datasource.CoinListStreamDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    @Singleton
    fun provideCoinListStreamDataSource(
        webSocketClient: BinanceWebSocketClient,
        subscriptionTable: SubscriptionTable,
        json: Json,
        @ApplicationScope scope: CoroutineScope,
    ): CoinListStreamDataSource = CoinListStreamDataSource(
        webSocketClient = webSocketClient,
        subscriptionTable = subscriptionTable,
        json = json,
        scope = scope,
    )
}
