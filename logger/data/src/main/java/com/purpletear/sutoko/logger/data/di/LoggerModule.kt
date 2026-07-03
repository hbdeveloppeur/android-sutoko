package com.purpletear.sutoko.logger.data.di

import com.purpletear.sutoko.core.domain.logger.Logger
import com.purpletear.sutoko.logger.data.BuildConfig
import com.purpletear.sutoko.logger.data.remote.LoggerConfig
import com.purpletear.sutoko.logger.data.remote.NtfyRemoteLogger
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoggerModule {

    @Binds
    @Singleton
    abstract fun bindLogger(impl: NtfyRemoteLogger): Logger

    companion object {

        @Provides
        @Singleton
        fun provideLoggerConfig(): LoggerConfig {
            return LoggerConfig(
                exceptionChannel = BuildConfig.LOGGER_EXCEPTION_CHANNEL,
                logChannel = BuildConfig.LOGGER_LOG_CHANNEL
            )
        }
    }
}
