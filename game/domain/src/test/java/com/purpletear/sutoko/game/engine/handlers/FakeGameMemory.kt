package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.MemoryEntry
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
        override suspend fun load(gameId: String, upToChapterNumber: Int): Map<String, MemoryEntry> =
            emptyMap()

        override suspend fun save(gameId: String, memories: Map<String, MemoryEntry>) {}
        override suspend fun clear(gameId: String) {}
        override suspend fun delete(gameId: String) {}
        override fun observe(gameId: String): Flow<Map<String, String>> = flowOf(emptyMap())
        override suspend fun upsert(gameId: String, key: String, value: String, chapterNumber: Int) {}
    }
    val fakeProgressRepo = object : UserGameProgressRepository {
        override fun observe(gameId: String): Flow<UserGameProgress> =
            flowOf(UserGameProgress(gameId = gameId, currentChapterCode = "", normalizedChapterCode = ""))

        override suspend fun get(gameId: String): UserGameProgress =
            UserGameProgress(gameId = gameId, currentChapterCode = "", normalizedChapterCode = "")

        override suspend fun save(progress: UserGameProgress) {}
        override suspend fun delete(gameId: String) {}
    }
    return GameMemory(fakeMemoryRepo, fakeProgressRepo).apply {
        setCurrentChapterNumber(1)
    }
}
