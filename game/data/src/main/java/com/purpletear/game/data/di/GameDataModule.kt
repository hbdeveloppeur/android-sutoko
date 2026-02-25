package com.purpletear.game.data.di

import android.content.Context
import com.purpletear.game.data.download.GameDownloadManagerImpl
import com.purpletear.game.data.provider.AndroidGamePathProviderImpl
import com.purpletear.game.data.provider.GamePathProvider
import com.purpletear.game.data.remote.GameApi
import com.purpletear.game.data.remote.UserGameApi
import com.purpletear.game.data.repository.GameRepositoryImpl
import com.purpletear.sutoko.game.download.GameDownloadManager
import com.purpletear.sutoko.game.repository.GameRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import purpletear.fr.purpleteartools.TableOfSymbols
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PortalRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SutokoRetrofit

/**
 * Dagger Hilt module for providing Game data layer dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object GameDataModule {

    /**
     * Provides the Retrofit instance for portal.sutoko.app.
     *
     * @return The Portal Retrofit instance.
     */
    @Provides
    @Singleton
    @PortalRetrofit
    fun providePortalRetrofit(): Retrofit {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .cache(null)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://portal.sutoko.app/portal/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    /**
     * Provides the Retrofit instance for sutoko.com/api.
     *
     * @return The Sutoko Retrofit instance.
     */
    @Provides
    @Singleton
    @SutokoRetrofit
    fun provideSutokoRetrofit(): Retrofit {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .cache(null)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://sutoko.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    /**
     * Provides the GameApi implementation.
     *
     * @param retrofit The Portal Retrofit instance.
     * @return The GameApi implementation.
     */
    @Provides
    @Singleton
    fun provideGameApi(@PortalRetrofit retrofit: Retrofit): GameApi {
        return retrofit.create(GameApi::class.java)
    }

    /**
     * Provides the UserGameApi implementation.
     *
     * @param retrofit The Sutoko Retrofit instance.
     * @return The UserGameApi implementation.
     */
    @Provides
    @Singleton
    fun provideUserGameApi(@SutokoRetrofit retrofit: Retrofit): UserGameApi {
        return retrofit.create(UserGameApi::class.java)
    }

    /**
     * Provides the GameRepository implementation.
     *
     * @param gameApi The GameApi instance.
     * @param userGameApi The UserGameApi instance.
     * @return The GameRepository implementation.
     */
    @Provides
    @Singleton
    fun provideGameRepository(
        gameApi: GameApi,
        userGameApi: UserGameApi,
        tableOfSymbols: TableOfSymbols,
        @ApplicationContext context: Context
    ): GameRepository {
        return GameRepositoryImpl(gameApi, userGameApi, tableOfSymbols, context)
    }

    /**
     * Provides the GamePathProvider implementation.
     *
     * @param context The application context.
     * @return The GamePathProvider implementation.
     */
    @Provides
    @Singleton
    fun provideGamePathProvider(
        @ApplicationContext context: Context
    ): GamePathProvider {
        return AndroidGamePathProviderImpl(context)
    }

    /**
     * Provides the GameDownloadManager implementation.
     *
     * @param impl The implementation instance.
     * @return The GameDownloadManager interface.
     */
    @Provides
    @Singleton
    fun provideGameDownloadManager(
        impl: GameDownloadManagerImpl
    ): GameDownloadManager {
        return impl
    }

}
