package fr.purpletear.sutoko.di

import com.purpletear.sutoko.core.domain.helper.AppVersionProvider
import fr.purpletear.sutoko.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the AppVersionProvider interface that provides
 * the version code from the app's BuildConfig.
 */
@Singleton
class AppVersionProviderImpl @Inject constructor() : AppVersionProvider {
    /**
     * Get the application version code.
     *
     * @return The version code of the application from BuildConfig.VERSION_CODE.
     */
    override fun getVersionCode(): Int {
        return BuildConfig.VERSION_CODE
    }
}
