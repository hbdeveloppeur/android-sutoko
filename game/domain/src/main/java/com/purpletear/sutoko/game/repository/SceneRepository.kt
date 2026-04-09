package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.scene.Scene

/**
 * Repository for accessing scene configurations.
 * Scenes are immutable game data loaded from scenes.json.
 *
 * All operations are suspend functions and safe for concurrent use.
 */
interface SceneRepository {
    /**
     * Preload scenes for a specific game.
     * Performs file I/O on Dispatchers.IO internally.
     *
     * @param gameId The unique identifier for the game
     */
    suspend fun preload(gameId: String)

    /**
     * Get a scene by its ID.
     * Lock-free read from in-memory cache.
     *
     * @param sceneId The scene identifier
     * @return The Scene configuration, or null if not found
     */
    suspend fun getScene(sceneId: Int): Scene?

    /**
     * Clear cached scenes for the current game.
     */
    suspend fun clear()
}
