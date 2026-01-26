package io.soma.cryptobook.di

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageModule {

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        @BinanceNetwork okHttpClient: OkHttpClient,
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
                add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("coin_images").toOkioPath())
                    .maxSizeBytes(50L * 1024 * 1024) // 50MB
                    .build()
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.15) // 15% of app memory
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
