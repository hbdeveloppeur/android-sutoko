package com.purpletear.game.data.di

import android.content.Context
import androidx.room.Room
import com.purpletear.game.data.BuildConfig
import com.purpletear.game.data.database.GameDatabase
import com.purpletear.game.data.database.migrations.GameDatabaseMigrations
import com.purpletear.game.data.download.GameDownloadManagerImpl
import com.purpletear.game.data.local.dao.GameInstallationDao
import com.purpletear.game.data.local.dao.UserGameProgressDao
import com.purpletear.game.data.provider.AndroidGamePathProviderImpl
import com.purpletear.game.data.provider.GamePathProvider
import com.purpletear.game.data.remote.GameApi
import com.purpletear.game.data.remote.GamePortalApi
import com.purpletear.game.data.repository.ChapterGraphRepositoryImpl
import com.purpletear.game.data.repository.GameInstallationRepositoryImpl
import com.purpletear.game.data.repository.GameRepositoryImpl
import com.purpletear.game.data.repository.UserGameProgressRepositoryImpl
import com.purpletear.sutoko.game.repository.ChapterGraphRepository
import com.purpletear.ntfy.Ntfy
import com.purpletear.sutoko.game.download.GameDownloadManager
import com.purpletear.sutoko.game.repository.GameInstallationRepository
import com.purpletear.sutoko.game.repository.GameRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
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
     * Provides the GameDatabase instance.
     *
     * @param context The application context.
     * @return The GameDatabase instance.
     */
    @Provides
    @Singleton
    fun provideGameDatabase(@ApplicationContext context: Context): GameDatabase {
        return Room.databaseBuilder(
            context,
            GameDatabase::class.java,
            "game_database"
        )
            .addMigrations(*GameDatabaseMigrations.ALL)
            .apply {
                if (BuildConfig.DEBUG) {
                    fallbackToDestructiveMigration()
                }
            }
            .build()
    }

    /**
     * Provides the GameInstallationDao instance.
     *
     * @param database The GameDatabase instance.
     * @return The GameInstallationDao instance.
     */
    @Provides
    @Singleton
    fun provideGameInstallationDao(database: GameDatabase): GameInstallationDao {
        return database.gameInstallationDao()
    }

    /**
     * Provides the GameInstallationRepository implementation.
     *
     * @param gameInstallationDao The GameInstallationDao instance.
     * @return The GameInstallationRepository implementation.
     */
    @Provides
    @Singleton
    fun provideGameInstallationRepository(
        gameInstallationDao: GameInstallationDao
    ): GameInstallationRepository {
        return GameInstallationRepositoryImpl(gameInstallationDao)
    }

    /**
     * Provides the UserGameProgressDao instance.
     *
     * @param database The GameDatabase instance.
     * @return The UserGameProgressDao instance.
     */
    @Provides
    @Singleton
    fun provideUserGameProgressDao(database: GameDatabase): UserGameProgressDao {
        return database.userGameProgressDao()
    }

    /**
     * Provides the UserGameProgressRepository implementation.
     *
     * @param userGameProgressDao The UserGameProgressDao instance.
     * @return The UserGameProgressRepository implementation.
     */
    @Provides
    @Singleton
    fun provideUserGameProgressRepository(
        userGameProgressDao: UserGameProgressDao
    ): UserGameProgressRepository {
        return UserGameProgressRepositoryImpl(userGameProgressDao)
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
     * @param gameInstallationRepository The GameInstallationRepository instance.
     * @param context The application context.
     * @param ntfy The Ntfy instance.
     * @return The GameRepository implementation.
     */
    @Provides
    @Singleton
    fun provideGameRepository(
        gameApi: GameApi,
        gamePortalApi: GamePortalApi,
        gameInstallationRepository: GameInstallationRepository,
        @ApplicationContext context: Context,
        ntfy: Ntfy
    ): GameRepository {
        return GameRepositoryImpl(gameApi, gamePortalApi, gameInstallationRepository, context, ntfy)
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

    /**
     * Provides the ChapterGraphRepository implementation.
     *
     * @param impl The implementation instance.
     * @return The ChapterGraphRepository interface.
     */
    @Provides
    @Singleton
    fun provideChapterGraphRepository(
        impl: ChapterGraphRepositoryImpl
    ): ChapterGraphRepository {
        return impl
    }

}
