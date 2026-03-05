package io.soma.cryptobook.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.soma.cryptobook.coindetail.data.repository.CoinDetailRepositoryImpl
import io.soma.cryptobook.coindetail.domain.repository.CoinDetailRepository
import io.soma.cryptobook.core.data.realtime.ticker.InMemoryWsTickerTable
import io.soma.cryptobook.core.data.realtime.ticker.WsTickerTable
import io.soma.cryptobook.core.data.repository.ExchangeRateRepositoryImpl
import io.soma.cryptobook.core.domain.repository.CoinRepository
import io.soma.cryptobook.core.domain.repository.ExchangeRateRepository
import io.soma.cryptobook.home.data.repository.CoinRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCoinRepository(impl: CoinRepositoryImpl): CoinRepository

    @Binds
    @Singleton
    abstract fun bindExchangeRateRepository(
        impl: ExchangeRateRepositoryImpl,
    ): ExchangeRateRepository

    @Binds
    @Singleton
    abstract fun bindCoinDetailRepository(impl: CoinDetailRepositoryImpl): CoinDetailRepository

    @Binds
    @Singleton
    abstract fun bindWsTickerTable(impl: InMemoryWsTickerTable): WsTickerTable
}
