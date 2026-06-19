package com.purpletear.sutoko.game.service

/**
 * Resolves relative media storage paths into fully-qualified URLs.
 */
interface MediaUrlResolver {
    fun resolveBannerUrl(storagePath: String?): String?
}
