package com.purpletear.ntfy

/**
 * Ntfy interface for sending notifications via ntfy.sh
 * 
 * Inspired by x00/ntfy-php library
 */
interface Ntfy {

    /**
     * Starts an action
     */
    fun startAction(description: String)

    /**
     * Send a message to the default log channel
     *
     * @param message The notification message
     * @param data Optional additional data to include
     */
    fun send(message: String, data: Map<String, Any?>? = null)

    /**
     * Send a message to a specific channel
     *
     * @param message The notification message
     * @param channelId The channel ID to send to
     * @param data Optional additional data to include
     */
    fun send(message: String, channelId: String, data: Map<String, Any?>? = null)

    /**
     * Send an exception notification to the error channel
     *
     * @param throwable The exception to report
     * @param data Optional additional context data
     */
    fun exception(throwable: Throwable, data: Map<String, Any?>? = null)

    /**
     * Send an urgent notification to the urgent channel
     *
     * @param throwable The urgent exception/message
     * @param data Optional additional context data
     */
    fun urgent(throwable: Throwable, data: Map<String, Any?>? = null)

    /**
     * Send an urgent message to the urgent channel
     *
     * @param message The urgent message
     * @param data Optional additional context data
     */
    fun urgent(message: String, data: Map<String, Any?>? = null)
}
