package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.model.UserGameProgressEntity
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.repository.MemoryRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Creates a GameMemory instance with no-op fake repositories for unit tests.
 * Safe to use for handler tests that only read from memory state.
 */
fun createFakeGameMemory(): GameMemory {
    val fakeMemoryRepo = object : MemoryRepository {
        override suspend fun load(gameId: String): Map<String, String> = emptyMap()
        override suspend fun save(gameId: String, memories: Map<String, String>) {}
        override suspend fun clear(gameId: String) {}
        override suspend fun delete(gameId: String) {}
        override fun observe(gameId: String): Flow<Map<String, String>> = flowOf(emptyMap())
        override suspend fun upsert(gameId: String, key: String, value: String) {}
    }
    val fakeProgressRepo = object : UserGameProgressRepository {
        override fun observe(gameId: String): Flow<UserGameProgressEntity> =
            flowOf(UserGameProgressEntity(gameId = gameId, currentChapterCode = "", normalizedChapterCode = ""))

        override suspend fun get(gameId: String): UserGameProgressEntity =
            UserGameProgressEntity(gameId = gameId, currentChapterCode = "", normalizedChapterCode = "")

        override suspend fun save(progress: UserGameProgressEntity) {}
        override suspend fun delete(gameId: String) {}
    }
    return GameMemory(fakeMemoryRepo, fakeProgressRepo)
}
