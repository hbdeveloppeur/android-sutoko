package com.purpletear.sutoko.game.usecase

import com.purpletear.sutoko.game.engine.GameCheckpoint
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.model.chapter.MemoryEntry
import com.purpletear.sutoko.game.repository.GameProgressTransaction
import com.purpletear.sutoko.game.repository.MemoryRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class RestartChapterUseCaseTest {
    private val chapter = Chapter(story = "story", code = "3B", number = 3)
    private val checkpoint = GameCheckpoint("3B", "graph", "node", GameCheckpoint.Gate.TAP, emptyMap(), emptyList())
    private var progress = UserGameProgress("story", "3B", "3b", "Alex", checkpoint)
    private var memories = mapOf("earlier" to MemoryEntry("kept", 1), "current" to MemoryEntry("gone", 3), "future" to MemoryEntry("gone", 4))
    private var saveError: Exception? = null
    private val progressRepository = object : UserGameProgressRepository {
        override fun observe(gameId: String) = flowOf(progress)
        override suspend fun get(gameId: String) = progress
        override suspend fun save(progress: UserGameProgress) {
            saveError?.let { throw it }
            this@RestartChapterUseCaseTest.progress = progress
        }
        override suspend fun delete(gameId: String) = error("Must not delete the game's progress")
    }
    private val memoryRepository = object : MemoryRepository {
        override suspend fun load(gameId: String, upToChapterNumber: Int): Map<String, MemoryEntry> {
            memories = memories.filterValues { it.chapterNumber < upToChapterNumber }
            return memories
        }
        override suspend fun save(gameId: String, memories: Map<String, MemoryEntry>) = error("Not used")
        override suspend fun clear(gameId: String) = error("Must retain earlier chapters")
        override suspend fun delete(gameId: String) = error("Must retain earlier chapters")
        override fun observe(gameId: String) = flowOf(emptyMap<String, String>())
        override suspend fun upsert(gameId: String, key: String, value: String, chapterNumber: Int) = error("Not used")
    }
    private val transaction = GameProgressTransaction { block ->
        val originalProgress = progress
        val originalMemories = memories
        try {
            block()
        } catch (error: Throwable) {
            progress = originalProgress
            memories = originalMemories
            throw error
        }
    }
    private val useCase = RestartChapterUseCase(progressRepository, memoryRepository, transaction)

    @Test
    fun restartClearsCheckpointAndCurrentMemoriesButKeepsChapterNameAndEarlierChoices() = runTest {
        assertTrue(useCase("story", chapter).isSuccess)
        assertEquals(UserGameProgress("story", "3B", "3b", "Alex"), progress)
        assertEquals(mapOf("earlier" to MemoryEntry("kept", 1)), memories)
    }

    @Test
    fun staleChapterCannotResetAnotherChapter() = runTest {
        val original = memories
        assertTrue(useCase("story", chapter.copy(code = "2A", number = 2)).isFailure)
        assertEquals(checkpoint, progress.checkpoint)
        assertEquals(original, memories)
    }

    @Test
    fun wrongStoryAndInvalidChapterAreRejected() = runTest {
        assertTrue(useCase("other-story", chapter).isFailure)
        assertTrue(useCase("story", chapter.copy(number = 0)).isFailure)
        assertTrue(useCase("story", chapter.copy(code = "")).isFailure)
        assertEquals(checkpoint, progress.checkpoint)
    }

    @Test
    fun failureRollsBackMemoryDeletion() = runTest {
        val original = memories
        saveError = IllegalStateException("disk error")
        assertTrue(useCase("story", chapter).isFailure)
        assertEquals(original, memories)
        assertEquals(checkpoint, progress.checkpoint)
    }

    @Test
    fun cancellationPropagatesAndRollsBack() = runTest {
        val original = memories
        saveError = CancellationException("cancelled")
        try {
            useCase("story", chapter)
            fail("Cancellation must propagate")
        } catch (_: CancellationException) {
            assertEquals(original, memories)
            assertEquals(checkpoint, progress.checkpoint)
        }
    }
}
