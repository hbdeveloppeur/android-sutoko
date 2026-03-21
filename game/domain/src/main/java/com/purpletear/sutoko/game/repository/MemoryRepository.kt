package com.purpletear.sutoko.game.repository

import kotlinx.coroutines.flow.Flow

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

    /**
     * Deletes all memories for a specific game.
     * @param gameId The game identifier
     */
    suspend fun delete(gameId: String)

    /**
     * Observes all memories for a specific game.
     * Emits empty map if no memories exist.
     * @param gameId The game identifier
     * @return Flow of key-value pairs
     */
    fun observe(gameId: String): Flow<Map<String, String>>

    /**
     * Upserts a single memory for a specific game.
     * @param gameId The game identifier
     * @param key The memory key
     * @param value The memory value
     */
    suspend fun upsert(gameId: String, key: String, value: String)
}
