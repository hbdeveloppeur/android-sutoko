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

    /**
     * Returns the full URL for a Sutoko media file.
     * @param storagePath The storage path of the media file
     * @return The full URL to access the media file
     */
    fun getSutokoMediaUrl(storagePath: String): String
}