package com.purpletear.sutoko.game.model.chapter

import com.purpletear.sutoko.game.engine.handlers.createFakeGameMemory
import org.junit.Assert.assertEquals
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
}
