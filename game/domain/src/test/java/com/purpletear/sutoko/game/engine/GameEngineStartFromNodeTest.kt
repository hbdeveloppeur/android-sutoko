package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.engine.handlers.BackgroundNodeHandler
import com.purpletear.sutoko.game.engine.handlers.ChapterChangeNodeHandler
import com.purpletear.sutoko.game.engine.handlers.ConditionNodeHandler
import com.purpletear.sutoko.game.engine.handlers.ConversationModeChangeNodeHandler
import com.purpletear.sutoko.game.engine.handlers.EndNodeHandler
import com.purpletear.sutoko.game.engine.handlers.InfoNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MemoryNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MessageImageNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MessageNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MessageThemeNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MessageVocalNodeHandler
import com.purpletear.sutoko.game.engine.handlers.CodeNodeHandler
import com.purpletear.sutoko.game.engine.handlers.IntroSentenceNodeHandler
import com.purpletear.sutoko.game.engine.handlers.SceneNodeHandler
import com.purpletear.sutoko.game.engine.handlers.SoundNodeHandler
import com.purpletear.sutoko.game.engine.handlers.StartNodeHandler
import com.purpletear.sutoko.game.engine.handlers.TrophyNodeHandler
import com.purpletear.sutoko.game.engine.handlers.createFakeGameMemory
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.repository.FakeCharacterRepository
import com.purpletear.sutoko.game.engine.processing.TextProcessorImpl
import com.purpletear.sutoko.game.engine.timing.FakeTimingScheduler
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineStartFromNodeTest {

    private val fakeTimingScheduler = FakeTimingScheduler()
    private val nodeResolver = NodeResolver()
    private val textProcessor = TextProcessorImpl()
    private val fakeCharacterRepository = FakeCharacterRepository()

    @Test
    fun `startFromNode - should execute target node`() = runBlocking {
        val engine = createEngine()
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "mid" to Node.Message(id = "mid", text = "Middle", characterId = 1)
            ),
            edges = listOf(
                Edge(source = "start", target = "mid")
            ),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        engine.startFromNode("mid")

        val textMessages = engine.messages.value.filterIsInstance<GameMessageText>()
        assertEquals(1, textMessages.size)
        assertEquals("Middle", textMessages.first().text)
    }

    @Test
    fun `startFromNode - should clear previous messages`() = runBlocking {
        val engine = createEngine()
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "msg1" to Node.Message(id = "msg1", text = "First", characterId = 1),
                "msg2" to Node.Message(id = "msg2", text = "Second", characterId = 1)
            ),
            edges = emptyList(),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        engine.startFromNode("msg1")

        assertEquals(1, engine.messages.value.filterIsInstance<GameMessageText>().size)

        engine.startFromNode("msg2")

        val textMessages = engine.messages.value.filterIsInstance<GameMessageText>()
        assertEquals(1, textMessages.size)
        assertEquals("Second", textMessages.first().text)
    }

    @Test(expected = IllegalStateException::class)
    fun `startFromNode without initialize - should throw`() = runBlocking {
        val engine = createEngine()
        engine.startFromNode("start")
    }

    @Test(expected = IllegalStateException::class)
    fun `startFromNode with unknown node - should throw`() = runBlocking {
        val engine = createEngine()
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf("start" to Node.Start(id = "start")),
            edges = emptyList(),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        engine.startFromNode("missing")
    }

    private fun createEngine(memory: GameMemory = createFakeGameMemory()): GameEngine {
        return GameEngine(
            handlerFactory = NodeHandlerFactory(
                startHandler = StartNodeHandler(),
                messageHandler = MessageNodeHandler(textProcessor),
                messageThemeHandler = MessageThemeNodeHandler(),
                messageImageHandler = MessageImageNodeHandler(),
                chapterChangeHandler = ChapterChangeNodeHandler(),
                conditionHandler = ConditionNodeHandler(),
                memoryHandler = MemoryNodeHandler(),
                infoHandler = InfoNodeHandler(textProcessor),
                trophyHandler = TrophyNodeHandler(),
                backgroundHandler = BackgroundNodeHandler(),
                conversationModeChangeHandler = ConversationModeChangeNodeHandler(),
                sceneHandler = SceneNodeHandler(),
                endHandler = EndNodeHandler(),
                soundHandler = SoundNodeHandler(),
                messageVocalHandler = MessageVocalNodeHandler(),
                codeHandler = CodeNodeHandler(),
                introSentenceHandler = IntroSentenceNodeHandler()
            ),
            nodeResolver = nodeResolver,
            memory = memory,
            timingScheduler = fakeTimingScheduler,
            textProcessor = textProcessor,
            characterRepository = fakeCharacterRepository
        )
    }
}
