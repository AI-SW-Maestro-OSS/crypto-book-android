package io.soma.cryptobook.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.soma.cryptobook.core.domain.manager.BuildInfoManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BuildInfoModule {

    @Provides
    @Singleton
    fun provideBuildInfoManager(@ApplicationContext context: Context): BuildInfoManager =
        object : BuildInfoManager {
            override val versionName: String
                get() = context.packageManager
                    .getPackageInfo(context.packageName, 0)
                    .versionName.orEmpty()
        }
}
