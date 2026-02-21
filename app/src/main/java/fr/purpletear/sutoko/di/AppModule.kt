package fr.purpletear.sutoko.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.purpletear.core.presentation.services.MakeToastService
import com.purpletear.sutoko.core.domain.helper.AppVersionProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.purpletear.sutoko.shop.coinsLogic.Customer
import purpletear.fr.purpleteartools.DelayHandler
import purpletear.fr.purpleteartools.TableOfPlayersV2
import purpletear.fr.purpleteartools.TableOfSymbols
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Singleton
    @Provides
    fun provideToastService(@ApplicationContext context: Context): MakeToastService {
        return MakeToastService(context)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(app: Application): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(app)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideDelayHandler(): DelayHandler {
        return DelayHandler()
    }

    @Provides
    @Singleton
    fun provideSoundPlayer(
        @ApplicationContext appContext: Context,
        delayHandler: DelayHandler
    ): TableOfPlayersV2 {
        return TableOfPlayersV2(
            context = appContext,
            delayHandler = delayHandler
        )
    }

    @Provides
    @Singleton
    fun provideTableOfSymbols(@ApplicationContext appContext: Context): TableOfSymbols {
        val symbols = TableOfSymbols(-1)
        symbols.read(appContext)
        return symbols
    }

    @Provides
    @Singleton
    fun provideCustomer(@ApplicationContext appContext: Context): Customer {
        return Customer(appContext, callbacks = null)
    }

    @Provides
    @Singleton
    fun provideAppVersionProvider(): AppVersionProvider {
        return AppVersionProviderImpl()
    }
}
