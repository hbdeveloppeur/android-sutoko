package com.purpletear.game.data.di

import android.content.Context
import androidx.room.Room
import com.purpletear.game.data.database.GameDatabase
import com.purpletear.game.data.database.migrations.GameDatabaseMigrations
import com.purpletear.game.data.file.GameFileManager
import com.purpletear.game.data.file.GameFileManagerImpl
import com.purpletear.game.data.infrastructure.SystemTimingScheduler
import com.purpletear.game.data.local.dao.GameDao
import com.purpletear.game.data.local.dao.GameInstallationDao
import com.purpletear.game.data.local.dao.MemoryDao
import com.purpletear.game.data.local.dao.UserGameProgressDao
import com.purpletear.game.data.provider.AndroidGamePathProviderImpl
import com.purpletear.game.data.remote.GameApi
import com.purpletear.game.data.remote.testing.TestEventDataSourceImpl
import com.purpletear.game.data.remote.testing.TestSessionApi
import com.purpletear.game.data.repository.testing.TestChapterGraphRepositoryImpl
import com.purpletear.game.data.repository.testing.TestPackageRepositoryImpl
import com.purpletear.game.data.repository.testing.TestSessionRepositoryImpl
import com.purpletear.sutoko.core.domain.helper.provider.HostProvider
import com.purpletear.game.data.repository.ChapterGraphRepositoryImpl
import com.purpletear.game.data.repository.CharacterRepositoryImpl
import com.purpletear.game.data.repository.GameInstallRepositoryImpl
import com.purpletear.game.data.repository.GameRepositoryImpl
import com.purpletear.game.data.repository.MemoryRepositoryImpl
import com.purpletear.game.data.repository.SceneRepositoryImpl
import com.purpletear.game.data.repository.UserGameProgressRepositoryImpl
import com.purpletear.game.data.service.MediaUrlResolverImpl
import com.purpletear.sutoko.game.engine.processing.TextProcessor
import com.purpletear.sutoko.game.engine.processing.TextProcessorImpl
import com.purpletear.sutoko.game.engine.timing.TimingScheduler
import com.purpletear.sutoko.game.repository.ChapterGraphRepository
import com.purpletear.sutoko.game.repository.CharacterRepository
import com.purpletear.sutoko.game.repository.MemoryRepository
import com.purpletear.sutoko.game.repository.SceneRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import com.purpletear.sutoko.game.repository.testing.TestChapterGraphRepository
import com.purpletear.sutoko.game.repository.testing.TestEventDataSource
import com.purpletear.sutoko.game.repository.testing.TestPackageRepository
import com.purpletear.sutoko.game.repository.testing.TestSessionRepository
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
     * Provides the TimingScheduler implementation.
     */
    @Provides
    @Singleton
    fun provideTimingScheduler(): TimingScheduler = SystemTimingScheduler()

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

    /**
     * Provides a shared OkHttp client for SSE and package downloads.
     */
    @Provides
    @Singleton
    @TestingOkHttpClient
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(null)
            .build()
    }

    /**
     * Provides the base URL for the real-time testing API.
     */
    @Provides
    @Singleton
    @TestingBaseUrl
    fun provideTestingBaseUrl(): String = "https://canvas.sutoko.com/api/"

    /**
     * Provides the Retrofit instance for the testing API.
     */
    @Provides
    @Singleton
    @TestingApi
    fun provideTestingRetrofit(@TestingBaseUrl baseUrl: String): Retrofit {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .cache(null)
            .build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    /**
     * Provides the TestSessionApi implementation.
     */
    @Provides
    @Singleton
    fun provideTestSessionApi(@TestingApi retrofit: Retrofit): TestSessionApi {
        return retrofit.create(TestSessionApi::class.java)
    }

    /**
     * Provides the TestSessionRepository implementation.
     */
    @Provides
    @Singleton
    fun provideTestSessionRepository(
        impl: TestSessionRepositoryImpl
    ): TestSessionRepository {
        return impl
    }

    /**
     * Provides the TestPackageRepository implementation.
     */
    @Provides
    @Singleton
    fun provideTestPackageRepository(
        impl: TestPackageRepositoryImpl
    ): TestPackageRepository {
        return impl
    }

    /**
     * Provides the TestEventDataSource implementation.
     */
    @Provides
    @Singleton
    fun provideTestEventDataSource(
        impl: TestEventDataSourceImpl
    ): TestEventDataSource {
        return impl
    }

    /**
     * Provides the TestChapterGraphRepository implementation.
     */
    @Provides
    @Singleton
    fun provideTestChapterGraphRepository(
        impl: TestChapterGraphRepositoryImpl
    ): TestChapterGraphRepository {
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
    abstract fun bindGameFileManager(impl: GameFileManagerImpl): GameFileManager

    @Binds
    abstract fun bindMediaUrlResolver(impl: MediaUrlResolverImpl): MediaUrlResolver
}
