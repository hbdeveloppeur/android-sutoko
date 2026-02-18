package com.purpletear.sutoko.core.domain.helper.provider

/**
 * Interface for providing the host name for API requests.
 * This interface is used across multiple modules to provide the host name for their respective API requests.
 */
interface HostProvider {
    /**
     * The host name for API requests.
     */
    val hostName: String

    fun getPublicMedia(filename: String): String
}