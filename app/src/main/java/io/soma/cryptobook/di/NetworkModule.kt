package io.soma.cryptobook.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.soma.cryptobook.core.data.network.ExchangeApiService
import io.soma.cryptobook.core.network.BinanceWebSocketClient
import io.soma.cryptobook.home.data.network.BinanceApiService
import io.soma.cryptobook.splash.data.network.CryptoBookApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BinanceNetwork

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CryptoBookNetwork

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class KoreaEximNetwork

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BINANCE_BASE_URL = "https://api.binance.com/"
    private const val CRYPTOBOOK_BASE_URL = "https://cryptobook.soma.io/"
    private const val KOREA_EXIM_BASE_URL = "https://oapi.koreaexim.go.kr/"

    @Provides
    @Singleton
    @BinanceNetwork
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .pingInterval(3, TimeUnit.MINUTES)
        .build()

    @Provides
    @Singleton
    fun providesNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    @BinanceNetwork
    fun provideBinanceRetrofit(@BinanceNetwork okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BINANCE_BASE_URL)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideBinanceApiService(@BinanceNetwork retrofit: Retrofit): BinanceApiService =
        retrofit.create(BinanceApiService::class.java)

    @Provides
    @Singleton
    fun provideBinanceWebSocketClient(
        @BinanceNetwork okHttpClient: OkHttpClient,
        @ApplicationScope scope: CoroutineScope,
    ): BinanceWebSocketClient = BinanceWebSocketClient(okHttpClient, scope)

// ========================================================================

    @Provides
    @Singleton
    @CryptoBookNetwork
    fun provideCryptoBookOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .build()

    @Provides
    @Singleton
    @CryptoBookNetwork
    fun provideCryptoBookRetrofit(
        @CryptoBookNetwork okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(CRYPTOBOOK_BASE_URL)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideCryptoBookApiService(@CryptoBookNetwork retrofit: Retrofit): CryptoBookApiService =
        retrofit.create(CryptoBookApiService::class.java)

// ========================================================================

    @Provides
    @Singleton
    @KoreaEximNetwork
    fun provideKoreaEximOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .build()

    @Provides
    @Singleton
    @KoreaEximNetwork
    fun provideKoreaEximRetrofit(
        @KoreaEximNetwork okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(KOREA_EXIM_BASE_URL)
            .addConverterFactory(json.asConverterFactory((contentType)))
            .build()
    }

    @Provides
    @Singleton
    fun provideExchangeApiService(@KoreaEximNetwork retrofit: Retrofit): ExchangeApiService =
        retrofit.create(ExchangeApiService::class.java)
}
