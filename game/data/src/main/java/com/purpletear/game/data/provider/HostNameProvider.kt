package com.purpletear.game.data.provider

/**
 * Interface for providing the host name for API requests.
 */
interface HostNameProvider {
    /**
     * Returns the base URL for API requests.
     *
     * @return The base URL as a string.
     */
    fun getHostName(): String
}