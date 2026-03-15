package com.purpletear.game.data.repository

import com.purpletear.game.data.local.dao.MemoryDao
import com.purpletear.game.data.local.entity.MemoryEntity
import com.purpletear.sutoko.game.repository.MemoryRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of MemoryRepository using Room database.
 */
@Singleton
class MemoryRepositoryImpl @Inject constructor(
    private val memoryDao: MemoryDao
) : MemoryRepository {
    
    override suspend fun load(gameId: String): Map<String, String> {
        return memoryDao.getAllForGame(gameId)
            .associate { it.key to it.value }
    }
    
    override suspend fun save(gameId: String, memories: Map<String, String>) {
        val entities = memories.map { (key, value) ->
            MemoryEntity(gameId = gameId, key = key, value = value)
        }
        memoryDao.insertAll(entities)
    }
    
    override suspend fun clear(gameId: String) {
        memoryDao.deleteAllForGame(gameId)
    }
}
