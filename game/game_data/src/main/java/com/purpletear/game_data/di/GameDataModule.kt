package com.purpletear.game_data.di

import android.content.Context
import com.purpletear.game_data.provider.AndroidGamePathProviderImpl
import com.purpletear.game_data.provider.GamePathProvider
import com.purpletear.game_data.remote.GameApi
import com.purpletear.game_data.repository.GameRepositoryImpl
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
     * Provides the GameApi implementation.
     *
     * @return The GameApi implementation.
     */
    @Provides
    @Singleton
    fun provideGameApi(): GameApi {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .cache(null) // Explicitly disable cache
            .build()
        return Retrofit.Builder()
            .baseUrl("https://portal.sutoko.app/portal/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(GameApi::class.java)
    }

    /**
     * Provides the GameRepository implementation.
     *
     * @param gameApi The GameApi instance.
     * @return The GameRepository implementation.
     */
    @Provides
    @Singleton
    fun provideGameRepository(
        gameApi: GameApi,
        tableOfSymbols: TableOfSymbols,
        @ApplicationContext context: Context
    ): GameRepository {
        return GameRepositoryImpl(gameApi, tableOfSymbols, context)
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

}
