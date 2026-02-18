package com.purpletear.sutoko.version.infrastructure.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.purpletear.sutoko.version.infrastructure.remote.VersionApi
import com.purpletear.sutoko.version.infrastructure.repository.VersionRepositoryImpl
import com.purpletear.sutoko.version.repository.VersionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VersionInfrastructureModule {


    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor { message ->
            android.util.Log.d("VersionApiInterceptor", message)
        }.apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideVersionApi(gson: Gson, client: OkHttpClient): VersionApi {
        return Retrofit.Builder()
            .baseUrl("https://portal.sutoko.app/portal/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
            .create(VersionApi::class.java)
    }

    @Provides
    @Singleton
    fun provideVersionRepository(api: VersionApi): VersionRepository = VersionRepositoryImpl(api)
}
