package com.purpletear.game_data.provider

import com.purpletear.sutoko.core.domain.helper.provider.HostProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [HostProvider] that provides the host name for API requests.
 */
@Singleton
class HostProviderImpl @Inject constructor(override val hostName: String) : HostProvider {
    override fun getPublicMedia(media: String): String {
        return "$hostName/public/media/$media".replace("//", "/")
    }
}
