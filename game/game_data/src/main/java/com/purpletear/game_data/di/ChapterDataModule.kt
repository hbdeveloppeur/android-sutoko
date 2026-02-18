package com.purpletear.game_data.di

import android.content.Context
import com.purpletear.game_data.remote.ChapterApi
import com.purpletear.game_data.repository.ChapterRepositoryImpl
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
     * @param context The application context.
     * @return The ChapterRepository implementation.
     */
    @Provides
    @Singleton
    fun provideChapterRepository(
        chapterApi: ChapterApi,
        symbols: TableOfSymbols,
        @ApplicationContext context: Context
    ): ChapterRepository {
        return ChapterRepositoryImpl(chapterApi, symbols, context)
    }
}
