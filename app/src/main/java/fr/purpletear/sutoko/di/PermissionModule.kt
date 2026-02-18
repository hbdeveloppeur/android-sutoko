package fr.purpletear.sutoko.di

import com.purpletear.sutoko.permission.data.PermissionRepositoryImpl
import com.purpletear.sutoko.permission.domain.repository.PermissionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PermissionModule {

    @Provides
    @Singleton
    fun provideBillingImplRepository(): PermissionRepository {
        return PermissionRepositoryImpl()
    }
}