package com.purpletear.sutoko.di

import com.purpletear.sutoko.data.UserConfigRepositoryImpl
import com.purpletear.sutoko.data.remote.UserConfigApi
import com.purpletear.sutoko.domain.repository.UserConfigRepository
import com.purpletear.sutoko.domain.repository.UserRepository
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
object UserConfigModule {

    @Provides
    @Singleton
    fun provideUserConfigApi(): UserConfigApi {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://ai-conversation.sutoko.app/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(UserConfigApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserConfigRepository(
        api: UserConfigApi,
        userRepository: UserRepository,
    ): UserConfigRepository {
        return UserConfigRepositoryImpl(
            api = api,
            userRepository = userRepository,
        )
    }
}