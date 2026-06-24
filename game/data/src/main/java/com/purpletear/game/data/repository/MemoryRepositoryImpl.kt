package com.purpletear.game.data.repository

import com.purpletear.game.data.local.dao.MemoryDao
import com.purpletear.game.data.local.entity.MemoryEntity
import com.purpletear.sutoko.game.model.chapter.MemoryEntry
import com.purpletear.sutoko.game.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of MemoryRepository using Room database.
 */
@Singleton
class MemoryRepositoryImpl @Inject constructor(
    private val memoryDao: MemoryDao
) : MemoryRepository {

    override suspend fun load(gameId: String, upToChapterNumber: Int): Map<String, MemoryEntry> {
        memoryDao.deleteFromChapter(gameId, upToChapterNumber)
        return memoryDao.getAllForGameUpToChapter(gameId, upToChapterNumber)
            .associate { it.key to MemoryEntry(it.value, it.chapterNumber) }
    }

    override suspend fun save(gameId: String, memories: Map<String, MemoryEntry>) {
        val entities = memories.map { (key, entry) ->
            MemoryEntity(
                gameId = gameId,
                key = key,
                value = entry.value,
                chapterNumber = entry.chapterNumber
            )
        }
        memoryDao.insertAll(entities)
    }

    override suspend fun clear(gameId: String) {
        memoryDao.deleteAllForGame(gameId)
    }

    override suspend fun delete(gameId: String) {
        memoryDao.deleteAllForGame(gameId = gameId)
    }

    override fun observe(gameId: String): Flow<Map<String, String>> {
        return memoryDao.observeAllForGame(gameId)
            .map { entities -> entities.associate { it.key to it.value } }
    }

    override suspend fun upsert(gameId: String, key: String, value: String, chapterNumber: Int) {
        memoryDao.insert(
            MemoryEntity(
                gameId = gameId,
                key = key,
                value = value,
                chapterNumber = chapterNumber
            )
        )
    }
}
