package com.purpletear.game.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.room.Room
import com.purpletear.game.data.database.GameDatabase
import com.purpletear.game.data.database.migrations.GameDatabaseMigrations
import com.purpletear.game.data.file.GameFileManager
import com.purpletear.game.data.file.GameFileManagerImpl
import com.purpletear.game.data.infrastructure.SystemTimingScheduler
import com.purpletear.game.data.local.dao.GameDao
import com.purpletear.game.data.local.dao.GameFavoriteDao
import com.purpletear.game.data.local.dao.GameInstallationDao
import com.purpletear.game.data.local.dao.MemoryDao
import com.purpletear.game.data.local.dao.UserGameProgressDao
import com.purpletear.game.data.provider.AndroidGamePathProviderImpl
import com.purpletear.game.data.remote.GameApi
import com.purpletear.game.data.repository.ChapterGraphRepositoryImpl
import com.purpletear.game.data.repository.CharacterRepositoryImpl
import com.purpletear.game.data.repository.FavoriteGamesRepositoryImpl
import com.purpletear.game.data.repository.FriendzonedProgressRepositoryImpl
import com.purpletear.game.data.repository.GameInstallRepositoryImpl
import com.purpletear.game.data.repository.GameRepositoryImpl
import com.purpletear.game.data.repository.MemoryRepositoryImpl
import com.purpletear.game.data.repository.SceneRepositoryImpl
import com.purpletear.game.data.repository.UserGameProgressRepositoryImpl
import com.purpletear.game.data.repository.UserRoleRepositoryImpl
import com.purpletear.game.data.service.MediaUrlResolverImpl
import com.purpletear.sutoko.game.engine.processing.TextProcessor
import com.purpletear.sutoko.game.engine.processing.TextProcessorImpl
import com.purpletear.sutoko.game.engine.timing.TimingScheduler
import com.purpletear.sutoko.game.repository.ChapterGraphRepository
import com.purpletear.sutoko.game.repository.CharacterRepository
import com.purpletear.sutoko.game.repository.FriendzonedProgressRepository
import com.purpletear.sutoko.game.repository.MemoryRepository
import com.purpletear.sutoko.game.repository.SceneRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import com.purpletear.sutoko.game.repository.UserRoleRepository
import com.purpletear.sutoko.game.repository.game.FavoriteGamesRepository
import com.purpletear.sutoko.game.repository.game.GameInstallRepository
import com.purpletear.sutoko.game.repository.game.GameRepository
import com.purpletear.sutoko.game.service.MediaUrlResolver
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
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
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides the GameDao instance.
     *
     * @param database The GameDatabase instance.
     * @return The GameDao instance.
     */
    @Provides
    @Singleton
    fun provideGameDao(database: GameDatabase): GameDao {
        return database.gameDao()
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
     * Provides the GameFavoriteDao instance.
     *
     * @param database The GameDatabase instance.
     * @return The GameFavoriteDao instance.
     */
    @Provides
    @Singleton
    fun provideGameFavoriteDao(database: GameDatabase): GameFavoriteDao {
        return database.gameFavoriteDao()
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
            .baseUrl("https://sutoko.com/")
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
     * Provides the GamePathProvider implementation.
     *
     * @param context The application context.
     * @return The GamePathProvider implementation.
     */
    @Provides
    @Singleton
    fun provideGamePathProvider(
        @ApplicationContext context: Context
    ): com.purpletear.sutoko.game.provider.GamePathProvider {
        return AndroidGamePathProviderImpl(context)
    }

    /**
     * Provides the AndroidGamePathProvider implementation.
     *
     * @param context The application context.
     * @return The AndroidGamePathProvider implementation.
     */
    @Provides
    @Singleton
    fun provideAndroidGamePathProvider(
        @ApplicationContext context: Context
    ): com.purpletear.game.data.provider.AndroidGamePathProvider {
        return AndroidGamePathProviderImpl(context)
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

    /**
     * Provides the MemoryDao instance.
     *
     * @param database The GameDatabase instance.
     * @return The MemoryDao instance.
     */
    @Provides
    @Singleton
    fun provideMemoryDao(database: GameDatabase): MemoryDao {
        return database.memoryDao()
    }

    /**
     * Provides the MemoryRepository implementation.
     *
     * @param memoryDao The MemoryDao instance.
     * @return The MemoryRepository implementation.
     */
    @Provides
    @Singleton
    fun provideMemoryRepository(
        memoryDao: MemoryDao
    ): MemoryRepository {
        return MemoryRepositoryImpl(memoryDao)
    }

    /**
     * Provides the DataStore used for game-related user preferences (e.g. user role).
     */
    @Provides
    @Singleton
    fun provideGameDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            produceFile = { File(context.filesDir, "datastore/game_prefs.preferences_pb") }
        )
    }

    /**
     * Provides the TimingScheduler implementation.
     * Delegates to the @Singleton SystemTimingScheduler so the engine and any other
     * consumer (e.g. the game ViewModel driving hold-to-pause) share one instance.
     */
    @Provides
    @Singleton
    fun provideTimingScheduler(impl: SystemTimingScheduler): TimingScheduler = impl

    /**
     * Provides the TextProcessor implementation.
     */
    @Provides
    @Singleton
    fun provideTextProcessor(): TextProcessor = TextProcessorImpl()

    /**
     * Provides the SceneRepository implementation.
     *
     * @param impl The implementation instance.
     * @return The SceneRepository interface.
     */
    @Provides
    @Singleton
    fun provideSceneRepository(
        impl: SceneRepositoryImpl
    ): SceneRepository {
        return impl
    }

    /**
     * Provides the CharacterRepository implementation.
     *
     * @param impl The implementation instance.
     * @return The CharacterRepository interface.
     */
    @Provides
    @Singleton
    fun provideCharacterRepository(
        impl: CharacterRepositoryImpl
    ): CharacterRepository {
        return impl
    }

}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindGameInstallRepository(impl: GameInstallRepositoryImpl): GameInstallRepository

    @Binds
    @Singleton
    abstract fun bindGameRepository(impl: GameRepositoryImpl): GameRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteGamesRepository(
        impl: FavoriteGamesRepositoryImpl
    ): FavoriteGamesRepository

    @Binds
    @Singleton
    abstract fun bindGameFileManager(impl: GameFileManagerImpl): GameFileManager

    @Binds
    abstract fun bindMediaUrlResolver(impl: MediaUrlResolverImpl): MediaUrlResolver

    @Binds
    @Singleton
    abstract fun bindUserRoleRepository(impl: UserRoleRepositoryImpl): UserRoleRepository

    @Binds
    @Singleton
    abstract fun bindFriendzonedProgressRepository(
        impl: FriendzonedProgressRepositoryImpl
    ): FriendzonedProgressRepository

}
