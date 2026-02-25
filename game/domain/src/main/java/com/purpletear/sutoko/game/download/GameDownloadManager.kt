package com.purpletear.sutoko.game.download

import com.purpletear.sutoko.game.model.Game
import kotlinx.coroutines.flow.StateFlow

/**
 * Manager interface for handling game downloads.
 * Provides a clean API for starting, monitoring, and cancelling game downloads.
 */
interface GameDownloadManager {

    /**
     * Returns a StateFlow that emits the current download state for the specified game.
     * The StateFlow will emit [GameDownloadState.Idle] if no download is in progress.
     *
     * @param gameId The unique identifier of the game
     * @return StateFlow of the current download state
     */
    fun getDownloadState(gameId: String): StateFlow<GameDownloadState>

    /**
     * Initiates a download for the specified game.
     * If a download is already in progress for this game, this method does nothing.
     * The download includes: generating download link → downloading → extracting → setting game version.
     *
     * @param gameId The unique identifier of the game to download
     * @param isPremium Whether the game requires premium authentication
     * @param userId The user ID for authenticated downloads (required for premium games)
     * @param userToken The user token for authenticated downloads (required for premium games)
     * @throws GameDownloadForbiddenException if the user doesn't have permission to download
     */
    suspend fun downloadGame(
        gameId: String,
        isPremium: Boolean,
        userId: String? = null,
        userToken: String? = null
    )

    /**
     * Cancels an ongoing download for the specified game.
     * Does nothing if no download is in progress.
     *
     * @param gameId The unique identifier of the game
     */
    fun cancelDownload(gameId: String)

    /**
     * Cleans up resources associated with the specified game's download state.
     * Should be called when the download state is no longer needed (e.g., ViewModel cleared).
     *
     * @param gameId The unique identifier of the game
     */
    fun cleanup(gameId: String)

    /**
     * Sets the game version after successful download and extraction.
     * This should be called by the ViewModel after receiving [GameDownloadState.Completed].
     *
     * @param game The game to set version for
     */
    suspend fun setGameVersion(game: Game)

    /**
     * Resets the download state to [GameDownloadState.Idle] for the specified game.
     * This should be called when the download UI is dismissed or when a completed/error
     * download state should be cleared from memory.
     *
     * @param gameId The unique identifier of the game
     */
    fun resetState(gameId: String)
}
