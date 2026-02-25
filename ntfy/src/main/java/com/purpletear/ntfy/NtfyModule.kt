package com.purpletear.ntfy

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing Ntfy dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NtfyModule {

    /**
     * Provides the Ntfy interface implementation
     *
     * @param config The Ntfy configuration
     * @return Ntfy implementation (NtfyClient)
     */
    @Provides
    @Singleton
    fun provideNtfy(config: NtfyConfig): Ntfy {
        return NtfyClient(config)
    }

    /**
     * Provides the default Ntfy configuration
     * 
     * Note: This uses empty channel IDs by default. 
     * The consuming app should provide its own NtfyConfig with proper channel IDs.
     *
     * @return Default Ntfy configuration
     */
    @Provides
    @Singleton
    fun provideNtfyConfig(): NtfyConfig {
        return NtfyConfig(
            baseUrl = "https://ntfy.sh",
            logChannelId = BuildConfig.NTFY_LOG_CHANNEL,
            errorChannelId = BuildConfig.NTFY_ERROR_CHANNEL,
            urgentChannelId = BuildConfig.NTFY_URGENT_CHANNEL,
            silent = true,
            appName = BuildConfig.LIBRARY_PACKAGE_NAME.substringAfterLast(".")
        )
    }
}
