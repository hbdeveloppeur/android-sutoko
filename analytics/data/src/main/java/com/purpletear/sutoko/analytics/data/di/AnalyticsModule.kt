package com.purpletear.sutoko.analytics.data.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.purpletear.sutoko.analytics.data.firebase.FirebaseAnalyticsTracker
import com.purpletear.sutoko.core.domain.analytics.AnalyticsTracker
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsTracker(impl: FirebaseAnalyticsTracker): AnalyticsTracker

    companion object {

        @Provides
        @Singleton
        fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
            return FirebaseAnalytics.getInstance(context)
        }
    }
}
