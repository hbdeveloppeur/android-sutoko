package com.purpletear.game.data.di

import android.content.Context
import androidx.room.Room
import com.purpletear.game.data.database.GameDatabase
import com.purpletear.game.data.local.dao.ChapterDao
import com.purpletear.game.data.remote.ChapterApi
import com.purpletear.game.data.repository.ChapterRepositoryImpl
import com.purpletear.sutoko.game.repository.ChapterRepository
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
 * Dagger Hilt module for providing Chapter data layer dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object ChapterDataModule {

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
        ).build()
    }

    /**
     * Provides the ChapterDao instance.
     *
     * @param database The GameDatabase instance.
     * @return The ChapterDao instance.
     */
    @Provides
    @Singleton
    fun provideChapterDao(database: GameDatabase): ChapterDao {
        return database.chapterDao()
    }

    /**
     * Provides the ChapterApi implementation.
     *
     * @return The ChapterApi implementation.
     */
    @Provides
    @Singleton
    fun provideChapterApi(): ChapterApi {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .build()
        return Retrofit.Builder()
            .baseUrl("https://portal.sutoko.app/portal/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ChapterApi::class.java)
    }

    /**
     * Provides the ChapterRepository implementation.
     *
     * @param chapterApi The ChapterApi instance.
     * @param chapterDao The ChapterDao instance.
     * @param symbols The TableOfSymbols instance.
     * @param context The application context.
     * @return The ChapterRepository implementation.
     */
    @Provides
    @Singleton
    fun provideChapterRepository(
        chapterApi: ChapterApi,
        chapterDao: ChapterDao,
        symbols: TableOfSymbols,
        @ApplicationContext context: Context
    ): ChapterRepository {
        return ChapterRepositoryImpl(chapterApi, chapterDao, symbols, context)
    }
}
