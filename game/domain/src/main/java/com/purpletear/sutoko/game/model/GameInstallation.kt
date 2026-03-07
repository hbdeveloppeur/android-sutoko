package com.purpletear.sutoko.game.model

import androidx.annotation.Keep

/**
 * Represents the installation state of a game on the device.
 * This is a domain entity with no framework dependencies.
 *
 * @property gameId The unique identifier of the game
 * @property installedVersion The version string of the installed game (empty if not installed)
 * @property installedAt Timestamp when the game was installed
 */
@Keep
data class GameInstallation(
    val gameId: String = "",
    val installedVersion: String = "",
    val installedAt: Long = 0L
) {
    /**
     * Returns true if the game is installed (has a non-empty version)
     */
    val isInstalled: Boolean
        get() = installedVersion.isNotBlank() && installedVersion != "none"
}
