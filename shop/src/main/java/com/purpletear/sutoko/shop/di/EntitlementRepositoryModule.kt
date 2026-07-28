package com.purpletear.sutoko.shop.di

import com.purpletear.sutoko.shop.data.repository.EntitlementRepositoryImpl
import com.purpletear.sutoko.shop.domain.repository.EntitlementRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EntitlementRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEntitlementRepository(
        impl: EntitlementRepositoryImpl
    ): EntitlementRepository
}
