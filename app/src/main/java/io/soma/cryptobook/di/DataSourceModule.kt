package io.soma.cryptobook.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.soma.cryptobook.core.network.BinanceWebSocketClient
import io.soma.cryptobook.core.network.SubscriptionManager
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
        subscriptionManager: SubscriptionManager,
        json: Json,
        @ApplicationScope scope: CoroutineScope,
    ): CoinListStreamDataSource = CoinListStreamDataSource(
        webSocketClient = webSocketClient,
        subscriptionManager = subscriptionManager,
        json = json,
        scope = scope,
    )
}
