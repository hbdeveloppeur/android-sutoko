package com.purpletear.ntfy

/**
 * Configuration class for Ntfy client
 *
 * @property errorChannelId The channel ID for error notifications
 * @property logChannelId The channel ID for log notifications (default)
 * @property urgentChannelId The channel ID for urgent notifications
 * @property baseUrl The base URL for ntfy.sh server (default: https://ntfy.sh)
 * @property silent If true, exceptions during notification will be swallowed (default: false)
 * @property appName Optional app name to display in notification titles
 */
data class NtfyConfig(
    val errorChannelId: String = "",
    val logChannelId: String = "",
    val urgentChannelId: String = "",
    val baseUrl: String = "https://ntfy.sh",
    val silent: Boolean = false,
    val appName: String? = null
) {
}
