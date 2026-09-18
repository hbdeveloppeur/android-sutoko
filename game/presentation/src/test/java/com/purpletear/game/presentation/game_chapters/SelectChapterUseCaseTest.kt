package com.purpletear.game.presentation.game_chapters

import com.purpletear.game.presentation.game_preview.fakes.FakeMemoryRepository
import com.purpletear.game.presentation.game_preview.fakes.FakeUserGameProgressRepository
import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.model.chapter.MemoryEntry
import com.purpletear.sutoko.game.repository.GameProgressTransaction
import com.purpletear.sutoko.game.usecase.SelectChapterUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectChapterUseCaseTest {
    private val progressRepository = FakeUserGameProgressRepository()
    private val memoryRepository = FakeMemoryRepository()
    private val gameId = "story"
    private val decisions = mapOf(
        "trusted_friend" to MemoryEntry("true", 1),
        "chosen_path" to MemoryEntry("forest", 2),
    )

    @Test
    fun `opening the current chapter preserves decisions from earlier chapters`() = runTest {
        assertCurrentChapterPreservesProgress("3A")
    }

    @Test
    fun `current chapter comparison ignores case`() = runTest {
        assertCurrentChapterPreservesProgress("3a")
    }

    private suspend fun assertCurrentChapterPreservesProgress(savedCode: String) {
        val progress = UserGameProgress(
            gameId = gameId,
            currentChapterCode = savedCode,
            normalizedChapterCode = "3a",
            heroName = "Alex",
        )
        progressRepository.save(progress)
        memoryRepository.save(gameId, decisions)

        val result = SelectChapterUseCase(progressRepository, GameProgressTransaction { it() })(gameId, "3A")

        assertTrue(result.isSuccess)
        assertEquals(decisions, memoryRepository.load(gameId, 3))
        assertEquals(progress, progressRepository.get(gameId))
    }

    @Test
    fun `switching chapters preserves memories and the hero name`() = runTest {
        progressRepository.save(
            UserGameProgress(
                gameId = gameId,
                currentChapterCode = "3A",
                normalizedChapterCode = "3a",
                heroName = "Alex",
            )
        )
        memoryRepository.save(gameId, decisions)

        val result = SelectChapterUseCase(progressRepository, GameProgressTransaction { it() })(gameId, "2A")

        assertTrue(result.isSuccess)
        assertEquals(decisions, memoryRepository.load(gameId, 3))
        assertEquals("Alex", progressRepository.get(gameId).heroName)
        assertEquals("2A", progressRepository.get(gameId).currentChapterCode)
        assertEquals("2a", progressRepository.get(gameId).normalizedChapterCode)
    }
}
