package io.soma.cryptobook.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.soma.cryptobook.core.domain.image.CoinImageResolver
import io.soma.cryptobook.home.data.image.CoinImageResolverImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class CoinImageModule {

    @Binds
    abstract fun bindCoinImageResolver(impl: CoinImageResolverImpl): CoinImageResolver
}
