package com.purpletear.game.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.purpletear.game.data.local.dto.character.CharacterDto
import com.purpletear.game.data.mapper.CharacterMapper.toDomain
import com.purpletear.game.data.provider.AndroidGamePathProvider
import com.purpletear.sutoko.game.model.character.Character
import com.purpletear.sutoko.game.repository.CharacterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CharacterRepository that loads characters from characters.json files.
 * Caches characters in memory for fast lookups during gameplay.
 *
 * Thread-safety: All operations use Mutex for synchronization and
 * Dispatchers.IO for file operations.
 */
@Singleton
class CharacterRepositoryImpl @Inject constructor(
    private val pathProvider: AndroidGamePathProvider
) : CharacterRepository {

    private val gson = Gson()
    private val characterCache = ConcurrentHashMap<Int, Character>()
    private var currentGameId: String? = null
    private val mutex = Mutex()

    override suspend fun preload(gameId: String) {
        mutex.withLock {
            if (currentGameId == gameId && characterCache.isNotEmpty()) {
                return
            }

            withContext(Dispatchers.IO) {
                try {
                    val charactersFile = File(
                        pathProvider.getGamesDirectory(),
                        "$gameId/characters/characters.json"
                    )

                    if (!charactersFile.exists()) {
                        Log.w(TAG, "Characters file not found: ${charactersFile.absolutePath}")
                        clearInternal()
                        currentGameId = gameId
                        return@withContext
                    }

                    val characterDtos: List<CharacterDto> = charactersFile.bufferedReader().use { reader ->
                        gson.fromJson(reader, object : TypeToken<List<CharacterDto>>() {}.type)
                    }

                    characterCache.clear()
                    characterCache.putAll(characterDtos.map { it.toDomain() }.associateBy { it.id })
                    currentGameId = gameId

                    Log.d(TAG, "Loaded ${characterCache.size} characters for game $gameId")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load characters for game $gameId", e)
                    clearInternal()
                    currentGameId = gameId
                }
            }
        }
    }

    override suspend fun getCharacter(id: Int): Character? {
        return characterCache[id]
    }

    override suspend fun getAll(): List<Character> {
        return characterCache.values.toList()
    }

    override suspend fun clear() {
        mutex.withLock {
            clearInternal()
        }
    }

    private fun clearInternal() {
        characterCache.clear()
        currentGameId = null
    }

    companion object {
        private const val TAG = "CharacterRepositoryImpl"
    }
}
