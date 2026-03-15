package com.purpletear.sutoko.game.repository

/**
 * Repository for persisting game memory (variables set during gameplay).
 * Memory is saved at explicit points (chapter end, pause) not on every change.
 */
interface MemoryRepository {
    /**
     * Loads all memories for a specific game.
     * @param gameId The game identifier
     * @return Map of key-value pairs
     */
    suspend fun load(gameId: String): Map<String, String>

    /**
     * Saves all memories for a specific game.
     * Replaces any existing memories for this game.
     * @param gameId The game identifier
     * @param memories Map of key-value pairs to save
     */
    suspend fun save(gameId: String, memories: Map<String, String>)

    /**
     * Clears all memories for a specific game.
     * @param gameId The game identifier
     */
    suspend fun clear(gameId: String)
}
