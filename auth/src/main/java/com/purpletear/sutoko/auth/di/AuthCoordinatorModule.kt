package com.purpletear.sutoko.auth.di

import com.purpletear.sutoko.auth.coordinator.AuthCoordinator
import com.purpletear.sutoko.auth.coordinator.AuthCoordinatorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthCoordinatorModule {

    @Binds
    @Singleton
    abstract fun bindAuthCoordinator(impl: AuthCoordinatorImpl): AuthCoordinator
}
