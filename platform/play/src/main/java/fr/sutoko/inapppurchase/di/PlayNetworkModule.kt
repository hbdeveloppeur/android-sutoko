package fr.sutoko.inapppurchase.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.sutoko.inapppurchase.billing.VerificationApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayNetworkModule {

    private const val VERIFICATION_BASE_URL = "http://162.19.94.164:4001/"

    @Provides
    @Singleton
    @Named("verification")
    fun provideVerificationOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("verification")
    fun provideVerificationRetrofit(
        @Named("verification") client: OkHttpClient,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(VERIFICATION_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideVerificationApi(@Named("verification") retrofit: Retrofit): VerificationApi =
        retrofit.create(VerificationApi::class.java)
}
