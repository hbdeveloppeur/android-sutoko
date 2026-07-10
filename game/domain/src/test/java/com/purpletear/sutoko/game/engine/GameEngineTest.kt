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
import com.purpletear.sutoko.game.engine.handlers.MessageVocalNodeHandler
import com.purpletear.sutoko.game.engine.handlers.CodeNodeHandler
import com.purpletear.sutoko.game.engine.handlers.IntroSentenceNodeHandler
import com.purpletear.sutoko.game.engine.handlers.SceneNodeHandler
import com.purpletear.sutoko.game.engine.handlers.SoundNodeHandler
import com.purpletear.sutoko.game.engine.handlers.StartNodeHandler
import com.purpletear.sutoko.game.engine.handlers.TrophyNodeHandler
import com.purpletear.sutoko.game.engine.handlers.createFakeGameMemory
import com.purpletear.sutoko.game.engine.message.GameMessageInfo
import com.purpletear.sutoko.game.repository.FakeCharacterRepository
import com.purpletear.sutoko.game.engine.message.GameMessageNextChapter
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.engine.processing.TextProcessorImpl
import com.purpletear.sutoko.game.engine.timing.FakeTimingScheduler
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

class GameEngineTest {

    private val fakeTimingScheduler = FakeTimingScheduler()
    private val nodeResolver = NodeResolver()
    private val textProcessor = TextProcessorImpl()
    private val fakeCharacterRepository = FakeCharacterRepository()

    @Test
    fun `start node with multiple message targets - should emit ShowChoices and await input`() = runBlocking {
        val engine = createEngine()
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "choiceA" to Node.Message(id = "choiceA", text = "Option A", characterId = 1),
                "choiceB" to Node.Message(id = "choiceB", text = "Option B", characterId = 1)
            ),
            edges = listOf(
                Edge(source = "start", target = "choiceA", type = EdgeType.NORMAL),
                Edge(source = "start", target = "choiceB", type = EdgeType.NORMAL)
            ),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        engine.start()

        assertTrue(engine.state.value is GameEngineState.AwaitingInput)

        val showChoices = engine.effects.first { it is HandlerEffect.ShowChoices } as HandlerEffect.ShowChoices
        assertEquals(2, showChoices.choices.size)
        assertEquals("Option A", showChoices.choices[0].text)
        assertEquals("choiceA", showChoices.choices[0].nextNodeId)
    }

    @Test
    fun `submit valid choice - should resume at selected node`() = runBlocking {
        val engine = createEngine()
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "choiceA" to Node.Message(id = "choiceA", text = "Option A", characterId = 1),
                "choiceB" to Node.Message(id = "choiceB", text = "Option B", characterId = 1)
            ),
            edges = listOf(
                Edge(source = "start", target = "choiceA", type = EdgeType.NORMAL),
                Edge(source = "start", target = "choiceB", type = EdgeType.NORMAL)
            ),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        engine.start()

        assertTrue(engine.state.value is GameEngineState.AwaitingInput)

        engine.submitChoice("choiceB")

        assertTrue(engine.state.value !is GameEngineState.AwaitingInput)
        val textMessages = engine.messages.value.filterIsInstance<com.purpletear.sutoko.game.engine.message.GameMessageText>()
        assertTrue(textMessages.any { it.text == "Option B" })
    }

    @Test(expected = IllegalStateException::class)
    fun `submit choice while not awaiting input - should throw`() = runBlocking {
        val engine = createEngine()
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start")
            ),
            edges = emptyList(),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        engine.start()

        engine.submitChoice("invalid")
    }

    @Test(expected = IllegalStateException::class)
    fun `submit invalid choice - should throw`() = runBlocking {
        val engine = createEngine()
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "choiceA" to Node.Message(id = "choiceA", text = "Option A", characterId = 1),
                "choiceB" to Node.Message(id = "choiceB", text = "Option B", characterId = 1)
            ),
            edges = listOf(
                Edge(source = "start", target = "choiceA", type = EdgeType.NORMAL),
                Edge(source = "start", target = "choiceB", type = EdgeType.NORMAL)
            ),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        engine.start()

        engine.submitChoice("not-a-choice")
    }

    @Test
    fun `choice labels containing prenom - should be replaced with heroName`() = runBlocking {
        val memory = createFakeGameMemory()
        val engine = createEngine(memory = memory)
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "choiceA" to Node.Message(id = "choiceA", text = "Salut [prenom]", characterId = 1),
                "choiceB" to Node.Message(id = "choiceB", text = "[prenom] ?", characterId = 1)
            ),
            edges = listOf(
                Edge(source = "start", target = "choiceA", type = EdgeType.NORMAL),
                Edge(source = "start", target = "choiceB", type = EdgeType.NORMAL)
            ),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        memory.set("heroName", "Alex")
        engine.start()

        val showChoices = engine.effects.first { it is HandlerEffect.ShowChoices } as HandlerEffect.ShowChoices
        assertEquals("Salut Alex", showChoices.choices[0].text)
        assertEquals("Alex ?", showChoices.choices[1].text)
    }

    @Test
    fun `info node text containing prenom - should be replaced with heroName`() = runBlocking {
        val memory = createFakeGameMemory()
        val engine = createEngine(memory = memory)
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "info1" to Node.Info(id = "info1", text = "[prenom] entend quelque chose")
            ),
            edges = listOf(
                Edge(source = "start", target = "info1", type = EdgeType.NORMAL)
            ),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        memory.set("heroName", "Alex")
        engine.start()

        val infoMessages = engine.messages.value.filterIsInstance<GameMessageInfo>()
        assertTrue(infoMessages.any { it.text == "Alex entend quelque chose" })
    }

    @Test
    fun `message node with empty text - should not add GameMessageText`() = runBlocking {
        val engine = createEngine()
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "emptyMsg" to Node.Message(id = "emptyMsg", text = "", characterId = 1)
            ),
            edges = listOf(
                Edge(source = "start", target = "emptyMsg", type = EdgeType.NORMAL)
            ),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        engine.start()

        val textMessages = engine.messages.value.filterIsInstance<GameMessageText>()
        assertTrue(textMessages.isEmpty())
    }

    @Test
    fun `message node with blank text after substitution - should not add GameMessageText`() = runBlocking {
        val memory = createFakeGameMemory()
        val engine = createEngine(memory = memory)
        val graph = ChapterGraph(
            chapterCode = "1A",
            title = "Test",
            nodes = mapOf(
                "start" to Node.Start(id = "start"),
                "blankMsg" to Node.Message(id = "blankMsg", text = "[prenom]", characterId = 1)
            ),
            edges = listOf(
                Edge(source = "start", target = "blankMsg", type = EdgeType.NORMAL)
            ),
            startNodeId = "start"
        )

        engine.initialize("game-1", graph)
        memory.set("heroName", "")
        engine.start()

        val textMessages = engine.messages.value.filterIsInstance<GameMessageText>()
        assertTrue(textMessages.isEmpty())
    }

    @Test
    fun `chapter change node - should emit ChangeChapter effect`() = runBlocking {
        val engine = createEngine()
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

    private fun createEngine(memory: GameMemory = createFakeGameMemory()): GameEngine {
        return GameEngine(
            handlerFactory = NodeHandlerFactory(
                startHandler = StartNodeHandler(),
                messageHandler = MessageNodeHandler(textProcessor),
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
