package com.purpletear.news_data.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.purpletear.news_data.remote.NewsApi
import com.purpletear.news_data.remote.NewsApiWrapper
import com.purpletear.news_data.repository.NewsRepositoryImpl
import com.purpletear.sutoko.core.domain.helper.AppVersionProvider
import com.purpletear.sutoko.news.repository.NewsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing News data layer dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object NewsDataModule {

    /**
     * Provides a custom Gson instance configured for the application.
     *
     * @return The configured Gson instance.
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    /**
     * Provides the NewsApi implementation.
     *
     * @param gson The Gson instance for JSON conversion.
     * @return The NewsApi implementation.
     */
    @Provides
    @Singleton
    fun provideNewsApi(gson: Gson): NewsApi {
        val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor { message ->
            android.util.Log.d("NewsApiInterceptor", message)
        }
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://portal.sutoko.app/portal/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
            .create(NewsApi::class.java)
    }

    /**
     * Provides the NewsApiWrapper implementation.
     *
     * @param newsApi The NewsApi instance.
     * @return The NewsApiWrapper implementation.
     */
    @Provides
    @Singleton
    fun provideNewsApiWrapper(newsApi: NewsApi): NewsApiWrapper {
        return NewsApiWrapper(newsApi)
    }

    /**
     * Provides the NewsRepository implementation.
     *
     * @param apiWrapper The NewsApiWrapper instance.
     * @param appVersionProvider The AppVersionProvider instance.
     * @return The NewsRepository implementation.
     */
    @Provides
    @Singleton
    fun provideNewsRepository(
        apiWrapper: NewsApiWrapper,
        appVersionProvider: AppVersionProvider
    ): NewsRepository {
        return NewsRepositoryImpl(apiWrapper, appVersionProvider)
    }
}
