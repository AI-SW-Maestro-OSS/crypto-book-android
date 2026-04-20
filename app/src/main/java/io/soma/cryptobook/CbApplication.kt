package io.soma.cryptobook

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import dagger.hilt.android.HiltAndroidApp
import io.soma.cryptobook.core.data.realtime.market.MarketRealtimeCoordinator
import javax.inject.Inject

@HiltAndroidApp
class CbApplication : Application(), SingletonImageLoader.Factory {
    @Inject
    lateinit var imageLoader: dagger.Lazy<ImageLoader>

    @Inject
    lateinit var marketRealtimeCoordinator: dagger.Lazy<MarketRealtimeCoordinator>

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    marketRealtimeCoordinator.get().start()
                }

                override fun onStop(owner: LifecycleOwner) {
                    marketRealtimeCoordinator.get().stop()
                }
            },
        )
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader.get()
}
