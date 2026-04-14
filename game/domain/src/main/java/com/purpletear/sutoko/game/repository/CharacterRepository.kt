package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.character.Character

/**
 * Repository for accessing character configurations.
 * Characters are immutable game data loaded from characters/characters.json.
 */
interface CharacterRepository {
    /**
     * Preload characters for a specific game.
     * Performs file I/O on Dispatchers.IO internally.
     *
     * @param gameId The unique identifier for the game
     */
    suspend fun preload(gameId: String)

    /**
     * Get a character by its ID.
     * Lock-free read from in-memory cache.
     *
     * @param id The character identifier
     * @return The Character configuration, or null if not found
     */
    suspend fun getCharacter(id: Int): Character?

    /**
     * Get all loaded characters.
     */
    suspend fun getAll(): List<Character>

    /**
     * Clear cached characters for the current game.
     */
    suspend fun clear()
}
