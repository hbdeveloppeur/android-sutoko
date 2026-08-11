package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.engine.handlers.createFakeGameMemory
import com.purpletear.sutoko.game.engine.message.GameMessageNextChapter
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.engine.message.GameMessageTyping
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.EdgeType
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineMessageFlowTest {

    @Test
    fun `chapter change node - should emit ChangeChapter effect`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "chapterChange" to Node.ChapterChange(id = "chapterChange", chapterCode = "1B")
            ),
            edges = listOf(
                Edge(source = "start", target = "chapterChange", type = EdgeType.NORMAL)
            ),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)

        val effectJob = launch {
            val changeChapter = engine.effects.first { it is HandlerEffect.ChangeChapter } as HandlerEffect.ChangeChapter
            assertEquals("1B", changeChapter.chapterCode)
        }

        engine.start()
        effectJob.join()

        assertTrue(engine.state.value is GameEngineState.ChapterFinished)
        assertTrue(engine.messages.value.any { it is GameMessageNextChapter })
    }

    @Test
    fun `message-theme then message - should stamp colors onto the emitted text message`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "theme" to Node.MessageTheme(
                    id = "theme",
                    backgroundColor = "#FF2200",
                    foregroundColor = "#00FF00"
                ),
                "msg" to Node.Message(id = "msg", text = "Colored", characterId = 1)
            ),
            edges = listOf(
                Edge(source = "start", target = "theme", type = EdgeType.NORMAL),
                Edge(source = "theme", target = "msg", type = EdgeType.NORMAL)
            ),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        engine.start()

        val textMessages = engine.messages.value.filterIsInstance<GameMessageText>()
        assertEquals(1, textMessages.size)
        assertEquals("#FF2200", textMessages.first().backgroundColor)
        assertEquals("#00FF00", textMessages.first().foregroundColor)
    }

    @Test
    fun `sms message typing indicator is replaced in place by the final text`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = messageGraph(text = "Hello")

        engine.initialize("game-1", graph)
        engine.start()

        val typing = engine.messages.value.filterIsInstance<GameMessageTyping>()
        val texts = engine.messages.value.filterIsInstance<GameMessageText>()
        assertTrue("typing indicator must be replaced, never left behind", typing.isEmpty())
        assertTrue("final text must still be present after typing", texts.any { it.text == "Hello" })
    }

    @Test
    fun `sms hesitating message still resolves to the final text`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = messageGraph(text = "Hmm", isHesitating = true)

        engine.initialize("game-1", graph)
        engine.start()

        val typing = engine.messages.value.filterIsInstance<GameMessageTyping>()
        val texts = engine.messages.value.filterIsInstance<GameMessageText>()
        assertTrue(typing.isEmpty())
        assertTrue(texts.any { it.text == "Hmm" })
    }

    @Test
    fun `sms message - should park awaiting tap after text is shown`() = runBlocking {
        val memory = createFakeGameMemory()
        val engine = createTestGameEngine(memory = memory)
        val graph = messageGraph(text = "Hello")

        engine.initialize("game-1", graph)
        memory.set(GameMemory.TYPING_ANIMATION_KEY, "true")
        engine.start()

        assertTrue(engine.state.value is GameEngineState.AwaitingTap)
        val textMessages = engine.messages.value.filterIsInstance<GameMessageText>()
        assertTrue(textMessages.any { it.text == "Hello" })

        engine.advanceOnTap()

        assertTrue(engine.state.value is GameEngineState.ChapterFinished)
    }

    @Test
    fun `sms message - awaiting tap state should carry an auto-advance reading delay`() = runBlocking {
        val memory = createFakeGameMemory()
        val engine = createTestGameEngine(memory = memory)
        val graph = messageGraph(text = "Hello")

        engine.initialize("game-1", graph)
        memory.set(GameMemory.TYPING_ANIMATION_KEY, "true")
        engine.start()

        val state = engine.state.value as GameEngineState.AwaitingTap
        // "Hello" is 5 chars * 100ms, clamped to the 1000ms minimum.
        assertEquals(1000L, state.autoAdvanceAfterMs)
    }

    @Test
    fun `advanceOnTap while not awaiting tap - should be ignored`() = runBlocking {
        val engine = createTestGameEngine()
        val graph = startOnlyGraph()

        engine.initialize("game-1", graph)
        engine.start()

        assertTrue(engine.state.value !is GameEngineState.AwaitingTap)
        engine.advanceOnTap()
        assertTrue(engine.state.value !is GameEngineState.AwaitingTap)
    }
}
