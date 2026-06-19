package com.purpletear.game.data.service

import com.purpletear.sutoko.game.service.MediaUrlResolver
import javax.inject.Inject
import javax.inject.Named

class MediaUrlResolverImpl @Inject constructor(
    @param:Named("mediaBaseUrl") private val mediaBaseUrl: String
) : MediaUrlResolver {

    override fun resolveBannerUrl(storagePath: String?): String? {
        return storagePath?.let { "$mediaBaseUrl$it" }
    }
}
