package com.purpletear.sutoko.game.model.chapter

import com.purpletear.sutoko.game.engine.handlers.createFakeGameMemory
import com.purpletear.sutoko.game.model.UserGameProgress
import com.purpletear.sutoko.game.repository.MemoryRepository
import com.purpletear.sutoko.game.repository.UserGameProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameMemoryTest {

    @Test
    fun `conversationMode is SMS when typingAnimation is true`() {
        val memory = createFakeGameMemory()

        memory.set(GameMemory.TYPING_ANIMATION_KEY, "true")

        assertEquals(ConversationMode.SMS, memory.conversationMode)
    }

    @Test
    fun `conversationMode is IRL when typingAnimation is false`() {
        val memory = createFakeGameMemory()

        memory.set(GameMemory.TYPING_ANIMATION_KEY, "false")

        assertEquals(ConversationMode.IRL, memory.conversationMode)
    }

    @Test
    fun `typingAnimation true overrides conversation_mode IRL`() {
        val memory = createFakeGameMemory()

        memory.set(GameMemory.CONVERSATION_MODE_KEY, "IRL")
        memory.set(GameMemory.TYPING_ANIMATION_KEY, "true")

        assertEquals(ConversationMode.SMS, memory.conversationMode)
    }

    @Test
    fun `typingAnimation false overrides conversation_mode SMS`() {
        val memory = createFakeGameMemory()

        memory.set(GameMemory.CONVERSATION_MODE_KEY, "SMS")
        memory.set(GameMemory.TYPING_ANIMATION_KEY, "false")

        assertEquals(ConversationMode.IRL, memory.conversationMode)
    }

    @Test
    fun `conversationMode falls back to conversation_mode SMS when typingAnimation is missing`() {
        val memory = createFakeGameMemory()

        memory.set(GameMemory.CONVERSATION_MODE_KEY, "SMS")

        assertEquals(ConversationMode.SMS, memory.conversationMode)
    }

    @Test
    fun `conversationMode falls back to conversation_mode IRL when typingAnimation is missing`() {
        val memory = createFakeGameMemory()

        memory.set(GameMemory.CONVERSATION_MODE_KEY, "IRL")

        assertEquals(ConversationMode.IRL, memory.conversationMode)
    }

    @Test
    fun `conversationMode defaults to IRL when both keys are missing`() {
        val memory = createFakeGameMemory()

        assertEquals(ConversationMode.IRL, memory.conversationMode)
    }

    @Test
    fun `conversationMode falls back to conversation_mode when typingAnimation is invalid`() {
        val memory = createFakeGameMemory()

        memory.set(GameMemory.TYPING_ANIMATION_KEY, "maybe")
        memory.set(GameMemory.CONVERSATION_MODE_KEY, "IRL")

        assertEquals(ConversationMode.IRL, memory.conversationMode)
    }

    @Test
    fun `conversationMode defaults to IRL when typingAnimation and conversation_mode are invalid`() {
        val memory = createFakeGameMemory()

        memory.set(GameMemory.TYPING_ANIMATION_KEY, "maybe")
        memory.set(GameMemory.CONVERSATION_MODE_KEY, "unknown")

        assertEquals(ConversationMode.IRL, memory.conversationMode)
    }

    @Test
    fun `conversationMode ignores typingAnimation case`() {
        val memory = createFakeGameMemory()

        memory.set(GameMemory.TYPING_ANIMATION_KEY, "TRUE")

        assertEquals(ConversationMode.SMS, memory.conversationMode)
    }

    @Test
    fun `isMainCharacter returns true when character id matches main character`() {
        val memory = createFakeGameMemory()

        memory.setMainCharacterId(1)

        assertTrue(memory.isMainCharacter(1))
    }

    @Test
    fun `isMainCharacter returns false when character id does not match`() {
        val memory = createFakeGameMemory()

        memory.setMainCharacterId(1)

        assertFalse(memory.isMainCharacter(2))
    }

    @Test
    fun `isMainCharacter returns false when no main character is set`() {
        val memory = createFakeGameMemory()

        assertFalse(memory.isMainCharacter(1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `setMainCharacterId rejects non-positive id`() {
        val memory = createFakeGameMemory()

        memory.setMainCharacterId(0)
    }

    @Test
    fun `load seeds heroName from user progress`() = runTest {
        val progressRepository = object : UserGameProgressRepository {
            override fun observe(gameId: String): Flow<UserGameProgress> =
                flowOf(UserGameProgress(gameId = gameId, heroName = "Léa"))
            override suspend fun get(gameId: String): UserGameProgress =
                UserGameProgress(gameId = gameId, heroName = "Léa")
            override suspend fun save(progress: UserGameProgress) {}
            override suspend fun delete(gameId: String) {}
        }
        val memoryRepository = object : MemoryRepository {
            override suspend fun load(gameId: String, upToChapterNumber: Int): Map<String, MemoryEntry> =
                emptyMap()
            override suspend fun save(gameId: String, memories: Map<String, MemoryEntry>) {}
            override suspend fun clear(gameId: String) {}
            override suspend fun delete(gameId: String) {}
            override fun observe(gameId: String): Flow<Map<String, String>> = flowOf(emptyMap())
            override suspend fun upsert(gameId: String, key: String, value: String, chapterNumber: Int) {}
        }
        val memory = GameMemory(memoryRepository, progressRepository)
        memory.setCurrentChapterNumber(1)

        memory.load("game-1", 1)

        assertEquals("Léa", memory.get(GameMemory.HERO_NAME_KEY))
        assertEquals("Léa", memory.state.value[GameMemory.HERO_NAME_KEY])
    }

    @Test
    fun `save writes heroName to user progress`() = runTest {
        var savedProgress: UserGameProgress? = null
        val progressRepository = object : UserGameProgressRepository {
            override fun observe(gameId: String): Flow<UserGameProgress> =
                flowOf(UserGameProgress(gameId = gameId))
            override suspend fun get(gameId: String): UserGameProgress =
                UserGameProgress(gameId = gameId)
            override suspend fun save(progress: UserGameProgress) {
                savedProgress = progress
            }
            override suspend fun delete(gameId: String) {}
        }
        val memoryRepository = object : MemoryRepository {
            override suspend fun load(gameId: String, upToChapterNumber: Int): Map<String, MemoryEntry> =
                emptyMap()
            override suspend fun save(gameId: String, memories: Map<String, MemoryEntry>) {}
            override suspend fun clear(gameId: String) {}
            override suspend fun delete(gameId: String) {}
            override fun observe(gameId: String): Flow<Map<String, String>> = flowOf(emptyMap())
            override suspend fun upsert(gameId: String, key: String, value: String, chapterNumber: Int) {}
        }
        val memory = GameMemory(memoryRepository, progressRepository)
        memory.setCurrentChapterNumber(1)
        memory.setCurrentChapter("1A")

        memory.load("game-1", 1)
        memory.set(GameMemory.HERO_NAME_KEY, "Pierre")
        memory.save()

        assertEquals("Pierre", savedProgress?.heroName)
    }
}
