package com.purpletear.game.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.purpletear.game.data.local.dto.scene.SceneDto
import com.purpletear.game.data.mapper.SceneMapper.toDomain
import com.purpletear.game.data.provider.GamePathProvider
import com.purpletear.sutoko.game.model.scene.Scene
import com.purpletear.sutoko.game.repository.SceneRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SceneRepository that loads scenes from scenes.json files.
 * Caches scenes in memory for fast lookups during gameplay.
 *
 * Thread-safety: All operations use Mutex for synchronization and
 * Dispatchers.IO for file operations.
 */
@Singleton
class SceneRepositoryImpl @Inject constructor(
    private val pathProvider: GamePathProvider
) : SceneRepository {

    private val gson = Gson()
    private val sceneCache = ConcurrentHashMap<Int, Scene>()
    private var currentGameId: String? = null
    private val mutex = Mutex()

    override suspend fun preload(gameId: String) {
        mutex.withLock {
            if (currentGameId == gameId && sceneCache.isNotEmpty()) {
                return
            }

            withContext(Dispatchers.IO) {
                try {
                    val scenesFile = File(
                        pathProvider.getGamesDirectory(),
                        "$gameId/scenes/scenes.json"
                    )

                    if (!scenesFile.exists()) {
                        Log.w(TAG, "Scenes file not found: ${scenesFile.absolutePath}")
                        clearInternal()
                        currentGameId = gameId
                        return@withContext
                    }

                    val sceneDtos: List<SceneDto> = scenesFile.bufferedReader().use { reader ->
                        gson.fromJson(reader, object : TypeToken<List<SceneDto>>() {}.type)
                    }

                    sceneCache.clear()
                    sceneCache.putAll(sceneDtos.map { it.toDomain() }.associateBy { it.id })
                    currentGameId = gameId

                    Log.d(TAG, "Loaded ${sceneCache.size} scenes for game $gameId")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load scenes for game $gameId", e)
                    clearInternal()
                    currentGameId = gameId
                }
            }
        }
    }

    override suspend fun getScene(sceneId: Int): Scene? {
        return sceneCache[sceneId]
    }

    override suspend fun clear() {
        mutex.withLock {
            clearInternal()
        }
    }

    private fun clearInternal() {
        sceneCache.clear()
        currentGameId = null
    }

    companion object {
        private const val TAG = "SceneRepositoryImpl"
    }
}
