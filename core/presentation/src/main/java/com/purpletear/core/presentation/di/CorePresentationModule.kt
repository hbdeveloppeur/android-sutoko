package com.purpletear.core.presentation.di

import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.core.presentation.services.ToastService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CorePresentationModule {

    @Binds
    abstract fun bindToastService(impl: MakeToastService): ToastService
}
