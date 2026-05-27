package io.soma.cryptobook.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.soma.cryptobook.core.presentation.message.MessageHelper
import io.soma.cryptobook.main.presentation.message.MessageCommandSource
import io.soma.cryptobook.main.presentation.message.MessageHelperImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class MessageModule {

    @Binds
    abstract fun bindMessageHelper(impl: MessageHelperImpl): MessageHelper

    @Binds
    abstract fun bindMessageCommandSource(impl: MessageHelperImpl): MessageCommandSource
}
