package io.soma.cryptobook.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.soma.cryptobook.core.network.BinanceWebSocketClient
import io.soma.cryptobook.core.network.SubscriptionManager
import io.soma.cryptobook.core.network.table.WebSocketTableManager
import io.soma.cryptobook.home.data.datasource.CoinListStreamDataSource
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    @Singleton
    fun provideCoinListStreamDataSource(
        tableManager: WebSocketTableManager,
        subscriptionManager: SubscriptionManager,
        webSocketClient: BinanceWebSocketClient,
        json: Json,
    ): CoinListStreamDataSource = CoinListStreamDataSource(
        tableManager = tableManager,
        subscriptionManager = subscriptionManager,
        webSocketClient = webSocketClient,
        json = json,
    )
}