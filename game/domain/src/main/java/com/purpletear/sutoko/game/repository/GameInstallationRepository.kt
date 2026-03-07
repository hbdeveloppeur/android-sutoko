package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.GameInstallation
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing game installations.
 * Abstracts the storage mechanism from the domain layer.
 */
interface GameInstallationRepository {

    /**
     * Saves or updates the installation record for a game.
     *
     * @param gameId The unique identifier of the game
     * @param version The version string to save
     */
    suspend fun saveInstallation(gameId: String, version: String)

    /**
     * Gets the installation record for a game.
     *
     * @param gameId The unique identifier of the game
     * @return The GameInstallation, or null if not found
     */
    suspend fun getInstallation(gameId: String): GameInstallation?

    /**
     * Gets the installed version for a game.
     *
     * @param gameId The unique identifier of the game
     * @return The version string, or null if not installed
     */
    suspend fun getInstalledVersion(gameId: String): String?

    /**
     * Checks if a game is currently installed.
     *
     * @param gameId The unique identifier of the game
     * @return true if the game is installed, false otherwise
     */
    suspend fun isInstalled(gameId: String): Boolean

    /**
     * Observes the installation status of a game as a Flow.
     *
     * @param gameId The unique identifier of the game
     * @return Flow emitting true if installed, false otherwise
     */
    fun observeInstallationStatus(gameId: String): Flow<Boolean>

    /**
     * Observes the full installation record of a game as a Flow.
     *
     * @param gameId The unique identifier of the game
     * @return Flow emitting the GameInstallation or null
     */
    fun observeInstallation(gameId: String): Flow<GameInstallation?>

    /**
     * Removes the installation record for a game.
     *
     * @param gameId The unique identifier of the game
     */
    suspend fun removeInstallation(gameId: String)
}
