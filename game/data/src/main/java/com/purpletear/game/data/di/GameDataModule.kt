package com.purpletear.game.data.di

import android.content.Context
import com.purpletear.game.data.download.GameDownloadManagerImpl
import com.purpletear.game.data.provider.AndroidGamePathProviderImpl
import com.purpletear.game.data.provider.GamePathProvider
import com.purpletear.game.data.remote.GameApi
import com.purpletear.game.data.remote.GamePortalApi
import com.purpletear.game.data.repository.GameRepositoryImpl
import com.purpletear.ntfy.Ntfy
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
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing Game data layer dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object GameDataModule {

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
     * Provides the Retrofit instance for sutoko.com/portal.
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
            .baseUrl("https://sutoko.com/portal/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    /**
     * Provides the GameApi implementation.
     *
     * @param retrofit The Sutoko Retrofit instance.
     * @return The GameApi implementation.
     */
    @Provides
    @Singleton
    fun provideGameApi(@SutokoRetrofit retrofit: Retrofit): GameApi {
        return retrofit.create(GameApi::class.java)
    }

    /**
     * Provides the GamePortalApi implementation.
     *
     * @param retrofit The Portal Retrofit instance.
     * @return The GamePortalApi implementation.
     */
    @Provides
    @Singleton
    fun provideGamePortalApi(@PortalRetrofit retrofit: Retrofit): GamePortalApi {
        return retrofit.create(GamePortalApi::class.java)
    } 

    /**
     * Provides the GameRepository implementation.
     *
     * @param gameApi The GameApi instance.
     * @param gamePortalApi The GamePortalApi instance.
     * @param tableOfSymbols The TableOfSymbols instance.
     * @param ntfy The Ntfy instance.
     * @param context The application context.
     * @return The GameRepository implementation.
     */
    @Provides
    @Singleton
    fun provideGameRepository(
        gameApi: GameApi,
        gamePortalApi: GamePortalApi,
        tableOfSymbols: TableOfSymbols,
        ntfy: Ntfy,
        @ApplicationContext context: Context
    ): GameRepository {
        return GameRepositoryImpl(gameApi, gamePortalApi, tableOfSymbols, context, ntfy)
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
