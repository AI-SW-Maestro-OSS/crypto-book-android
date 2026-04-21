package io.soma.cryptobook.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.soma.cryptobook.coindetail.data.network.BinanceSpotKlineClient
import io.soma.cryptobook.coindetail.data.network.BinanceSpotTickerClient
import io.soma.cryptobook.core.data.datasource.ticksize.BinanceExchangeInfoApiService
import io.soma.cryptobook.core.data.network.ExchangeApiService
import io.soma.cryptobook.core.data.realtime.kline.WsKlineTable
import io.soma.cryptobook.core.data.realtime.market.DefaultMarketRealtimeCoordinator
import io.soma.cryptobook.core.data.realtime.market.DefaultObserveMarketRealtimeState
import io.soma.cryptobook.core.data.realtime.market.MarketRealtimeCoordinator
import io.soma.cryptobook.core.data.realtime.market.MarketRealtimePayloadMapper
import io.soma.cryptobook.core.data.realtime.ticker.WsTickerTable
import io.soma.cryptobook.core.domain.usecase.ObserveMarketRealtimeState
import io.soma.cryptobook.core.network.BinanceWebSocketClient
import io.soma.cryptobook.core.network.market.DefaultWsMarketMessageRouter
import io.soma.cryptobook.core.network.market.WsMarketMessageParser
import io.soma.cryptobook.core.network.market.WsMarketMessageRouter
import io.soma.cryptobook.core.network.session.DefaultWsSessionManager
import io.soma.cryptobook.core.network.session.WsSessionManager
import io.soma.cryptobook.core.network.session.WsSessionPolicy
import io.soma.cryptobook.core.network.subscription.DefaultWsSubscriptionManager
import io.soma.cryptobook.core.network.subscription.WsSubscriptionManager
import io.soma.cryptobook.core.network.subscription.WsSubscriptionPolicy
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
    fun provideDefaultOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @BinanceNetwork
    fun provideBinanceOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
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
    fun provideBinanceExchangeInfoApiService(
        @BinanceNetwork retrofit: Retrofit,
    ): BinanceExchangeInfoApiService = retrofit.create(BinanceExchangeInfoApiService::class.java)

    @Provides
    @Singleton
    fun provideBinanceSpotApiService(@BinanceNetwork retrofit: Retrofit): BinanceSpotApiService =
        retrofit.create(BinanceSpotApiService::class.java)

    @Provides
    @Singleton
    fun provideBinanceSpotKlineClient(apiService: BinanceSpotApiService): BinanceSpotKlineClient =
        DefaultBinanceSpotKlineClient(apiService)

    @Provides
    @Singleton
    fun provideBinanceSpotTickerClient(
        apiService: BinanceSpotApiService,
    ): BinanceSpotTickerClient = DefaultBinanceSpotTickerClient(apiService)

    @Provides
    @Singleton
    fun provideBinanceWebSocketClient(
        @BinanceNetwork okHttpClient: OkHttpClient,
    ): BinanceWebSocketClient = BinanceWebSocketClient(okHttpClient)

    @Provides
    @Singleton
    fun provideWsSessionPolicy(): WsSessionPolicy = WsSessionPolicy()

    @Provides
    @Singleton
    fun provideWsSessionManager(
        webSocketClient: BinanceWebSocketClient,
        @ApplicationScope scope: CoroutineScope,
        policy: WsSessionPolicy,
    ): WsSessionManager = DefaultWsSessionManager(
        transport = webSocketClient,
        scope = scope,
        policy = policy,
    )

    @Provides
    @Singleton
    fun provideWsSubscriptionPolicy(): WsSubscriptionPolicy = WsSubscriptionPolicy()

    @Provides
    @Singleton
    fun provideWsSubscriptionManager(
        sessionManager: WsSessionManager,
        webSocketClient: BinanceWebSocketClient,
        @ApplicationScope scope: CoroutineScope,
        policy: WsSubscriptionPolicy,
    ): WsSubscriptionManager = DefaultWsSubscriptionManager(
        sessionManager = sessionManager,
        transport = webSocketClient,
        scope = scope,
        policy = policy,
    )

    @Provides
    @Singleton
    fun provideWsMarketMessageParser(json: Json): WsMarketMessageParser =
        WsMarketMessageParser(json)

    @Provides
    @Singleton
    fun provideWsMarketMessageRouter(
        sessionManager: WsSessionManager,
        parser: WsMarketMessageParser,
        @ApplicationScope scope: CoroutineScope,
    ): WsMarketMessageRouter = DefaultWsMarketMessageRouter(
        sessionManager = sessionManager,
        parser = parser,
        scope = scope,
    )

    @Provides
    @Singleton
    fun provideMarketRealtimeCoordinator(
        sessionManager: WsSessionManager,
        subscriptionManager: WsSubscriptionManager,
        marketMessageRouter: WsMarketMessageRouter,
        tickerTable: WsTickerTable,
        klineTable: WsKlineTable,
        payloadMapper: MarketRealtimePayloadMapper,
        @ApplicationScope scope: CoroutineScope,
    ): MarketRealtimeCoordinator = DefaultMarketRealtimeCoordinator(
        sessionManager = sessionManager,
        subscriptionManager = subscriptionManager,
        marketMessageRouter = marketMessageRouter,
        tickerTable = tickerTable,
        klineTable = klineTable,
        payloadMapper = payloadMapper,
        scope = scope,
    )

    @Provides
    @Singleton
    fun provideObserveMarketRealtimeState(
        coordinator: MarketRealtimeCoordinator,
    ): ObserveMarketRealtimeState = DefaultObserveMarketRealtimeState(
        coordinator = coordinator,
    )

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
