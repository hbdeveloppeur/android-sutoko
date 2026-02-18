package fr.purpletear.sutoko.di

import com.purpletear.sutoko.notification.data.repository.NotificationRepositoryImpl
import com.purpletear.sutoko.notification.repository.NotificationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideNotificationRepository(): NotificationRepository {
        return NotificationRepositoryImpl()
    }
}