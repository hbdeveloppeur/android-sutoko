package com.purpletear.sutoko.core.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ActivityProviderModule {

    @Binds
    abstract fun bindActivityProvider(impl: DefaultActivityProvider): ActivityProvider
}
