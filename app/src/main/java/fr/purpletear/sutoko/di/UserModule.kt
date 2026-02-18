package fr.purpletear.sutoko.di

import android.content.Context
import com.purpletear.sutoko.data.repository.UserRepositoryImpl
import com.purpletear.sutoko.data.user.SharedPrefsReminderNotificationStateStore
import com.purpletear.sutoko.data.user.UserReminderSetter
import com.purpletear.sutoko.user.port.ReminderNotificationStateStore
import com.purpletear.sutoko.user.port.UserValuesUpdater
import com.purpletear.sutoko.user.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UserModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        @ApplicationContext context: Context,
        customer: Customer,
    ): UserRepository {
        customer.read(context)
        return UserRepositoryImpl(
            initialConnectionState = customer.isUserConnected(),
            customer = customer
        )
    }

    @Provides
    @Singleton
    fun provideUserValuesUpdater(
        @ApplicationContext context: Context,
    ): UserValuesUpdater = UserReminderSetter(context)

    @Provides
    @Singleton
    fun provideReminderNotificationStateStore(
        @ApplicationContext context: Context,
    ): ReminderNotificationStateStore = SharedPrefsReminderNotificationStateStore(context)
}