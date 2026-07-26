package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.model.chapter.MemoryEntry
import com.purpletear.sutoko.game.repository.FriendzonedProgressRepository
import com.purpletear.sutoko.game.repository.MemoryRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RestartGameUseCaseTest {

    private class FakeUserGameProgressRepository : UserGameProgressRepository {
        val deletedGameIds = mutableListOf<String>()

        override fun observe(gameId: String): Flow<UserGameProgress> =
            flowOf(UserGameProgress(gameId = gameId))

        override suspend fun get(gameId: String): UserGameProgress =
            UserGameProgress(gameId = gameId)

        override suspend fun save(progress: UserGameProgress) = Unit

        override suspend fun delete(gameId: String) {
            deletedGameIds.add(gameId)
        }
    }

    private class FakeMemoryRepository : MemoryRepository {
        val deletedGameIds = mutableListOf<String>()

        override suspend fun load(gameId: String, upToChapterNumber: Int): Map<String, MemoryEntry> =
            emptyMap()

        override suspend fun save(gameId: String, memories: Map<String, MemoryEntry>) = Unit

        override suspend fun clear(gameId: String) = Unit

        override suspend fun delete(gameId: String) {
            deletedGameIds.add(gameId)
        }

        override fun observe(gameId: String): Flow<Map<String, String>> = flowOf(emptyMap())

        override suspend fun upsert(gameId: String, key: String, value: String, chapterNumber: Int) =
            Unit
    }

    private class FakeFriendzonedProgressRepository : FriendzonedProgressRepository {
        val resetLegacyIds = mutableListOf<Int>()

        override suspend fun getChapterCode(legacyId: Int): String = "1a"

        override suspend fun reset(legacyId: Int) {
            resetLegacyIds.add(legacyId)
        }
    }

    private val progressRepository = FakeUserGameProgressRepository()
    private val memoryRepository = FakeMemoryRepository()
    private val friendzonedRepository = FakeFriendzonedProgressRepository()
    private val useCase = RestartGameUseCase(progressRepository, memoryRepository, friendzonedRepository)

    @Test
    fun `restart without legacyId keeps existing behavior and skips friendzoned reset`() = runTest {
        val result = useCase(gameId = "game-1")

        assertTrue(result.isSuccess)
        assertEquals(listOf("game-1"), progressRepository.deletedGameIds)
        assertEquals(listOf("game-1"), memoryRepository.deletedGameIds)
        assertTrue(friendzonedRepository.resetLegacyIds.isEmpty())
    }

    @Test
    fun `restart with non-friendzoned legacyId skips friendzoned reset`() = runTest {
        // 160 (SMS) runs on the standard engine; 42 is a regular legacy id.
        useCase(gameId = "game-1", legacyId = 160)
        useCase(gameId = "game-2", legacyId = 42)

        assertTrue(friendzonedRepository.resetLegacyIds.isEmpty())
        assertEquals(listOf("game-1", "game-2"), progressRepository.deletedGameIds)
    }

    @Test
    fun `restart with friendzoned legacyId also resets friendzoned progress`() = runTest {
        val result = useCase(gameId = "game-1", legacyId = 162)

        assertTrue(result.isSuccess)
        assertEquals(listOf(162), friendzonedRepository.resetLegacyIds)
        assertEquals(listOf("game-1"), progressRepository.deletedGameIds)
        assertEquals(listOf("game-1"), memoryRepository.deletedGameIds)
    }
}
