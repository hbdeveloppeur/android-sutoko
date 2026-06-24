package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.chapter.MemoryEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository for persisting game memory (variables set during gameplay).
 * Memory is saved at explicit points (chapter end, pause) not on every change.
 */
interface MemoryRepository {
    /**
     * Loads memories for a specific game up to and including the given chapter number.
     *
     * Memories written in [upToChapterNumber] or any later chapter are deleted first,
     * preventing stale state from future chapters from leaking into a replay.
     *
     * @param gameId The game identifier
     * @param upToChapterNumber The inclusive upper chapter-number bound
     * @return Map of key-value pairs, each tagged with the chapter it was written in
     */
    suspend fun load(gameId: String, upToChapterNumber: Int): Map<String, MemoryEntry>

    /**
     * Saves all memories for a specific game.
     * Replaces any existing memories for this game.
     *
     * @param gameId The game identifier
     * @param memories Map of key-value pairs to save, each tagged with its chapter number
     */
    suspend fun save(gameId: String, memories: Map<String, MemoryEntry>)

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
     *
     * @param gameId The game identifier
     * @return Flow of key-value pairs
     */
    fun observe(gameId: String): Flow<Map<String, String>>

    /**
     * Upserts a single memory for a specific game.
     *
     * @param gameId The game identifier
     * @param key The memory key
     * @param value The memory value
     * @param chapterNumber The chapter number in which the memory is written
     */
    suspend fun upsert(gameId: String, key: String, value: String, chapterNumber: Int)
}
