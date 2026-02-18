package fr.purpletear.sutoko.di

import android.content.SharedPreferences
import com.purpletear.shop.data.remote.CatalogApi
import com.purpletear.shop.data.remote.ShopApi
import com.purpletear.shop.data.repository.ShopRepositoryImpl
import com.purpletear.shop.domain.repository.ShopRepository
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
class ShopModule {

    @Provides
    @Singleton
    fun provideImageGenerationApi(): ShopApi {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://shop.sutoko.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ShopApi::class.java)
    }

    @Provides
    @Singleton
    fun provideShopRepository(
        api: ShopApi,
        sharedPreferences: SharedPreferences,
    ): ShopRepository {
        return ShopRepositoryImpl(
            api = api,
            sharedPreferences = sharedPreferences,
        )
    }

    @Provides
    @Singleton
    fun provideCatalogApi(): CatalogApi {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://catalog-shop.sutoko.app/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(CatalogApi::class.java)
    }
}
