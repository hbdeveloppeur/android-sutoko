package com.purpletear.sutoko.core.domain.helper

/**
 * Interface for providing the application version code.
 * This interface should be implemented by the app module to provide
 * the actual version code from the app's BuildConfig.
 */
interface AppVersionProvider {
    /**
     * Get the application version code.
     *
     * @return The version code of the application.
     */
    fun getVersionCode(): Int
}