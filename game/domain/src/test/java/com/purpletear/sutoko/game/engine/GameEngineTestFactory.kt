package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.engine.handlers.BackgroundNodeHandler
import com.purpletear.sutoko.game.engine.handlers.ChapterChangeNodeHandler
import com.purpletear.sutoko.game.engine.handlers.CodeNodeHandler
import com.purpletear.sutoko.game.engine.handlers.ConditionNodeHandler
import com.purpletear.sutoko.game.engine.handlers.ConversationModeChangeNodeHandler
import com.purpletear.sutoko.game.engine.handlers.EndNodeHandler
import com.purpletear.sutoko.game.engine.handlers.FakeNotificationNodeHandler
import com.purpletear.sutoko.game.engine.handlers.InfoNodeHandler
import com.purpletear.sutoko.game.engine.handlers.IntroSentenceNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MangaPageNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MemoryNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MessageAudioDialogueNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MessageImageNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MessageNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MessageThemeNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MessageVocalNodeHandler
import com.purpletear.sutoko.game.engine.handlers.SceneNodeHandler
import com.purpletear.sutoko.game.engine.handlers.SoundNodeHandler
import com.purpletear.sutoko.game.engine.handlers.StartNodeHandler
import com.purpletear.sutoko.game.engine.handlers.StopSoundNodeHandler
import com.purpletear.sutoko.game.engine.handlers.TrophyNodeHandler
import com.purpletear.sutoko.game.engine.handlers.VisualNovelNodeHandler
import com.purpletear.sutoko.game.engine.handlers.createFakeGameMemory
import com.purpletear.sutoko.game.engine.processing.TextProcessorImpl
import com.purpletear.sutoko.game.engine.timing.FakeTimingScheduler
import com.purpletear.sutoko.game.model.chapter.ChapterGraph
import com.purpletear.sutoko.game.model.chapter.Edge
import com.purpletear.sutoko.game.model.chapter.EdgeType
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import com.purpletear.sutoko.game.repository.CharacterRepository
import com.purpletear.sutoko.game.repository.FakeCharacterRepository

internal fun createTestGameEngine(
    memory: GameMemory = createFakeGameMemory(),
    characterRepository: CharacterRepository = FakeCharacterRepository()
): GameEngine {
    val textProcessor = TextProcessorImpl()
    return GameEngine(
        handlerFactory = NodeHandlerFactory(
            startHandler = StartNodeHandler(),
            messageHandler = MessageNodeHandler(textProcessor),
            messageThemeHandler = MessageThemeNodeHandler(),
            messageImageHandler = MessageImageNodeHandler(),
            mangaPageHandler = MangaPageNodeHandler(textProcessor),
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
            stopSoundHandler = StopSoundNodeHandler(),
            messageVocalHandler = MessageVocalNodeHandler(),
            messageAudioDialogueHandler = MessageAudioDialogueNodeHandler(),
            codeHandler = CodeNodeHandler(),
            introSentenceHandler = IntroSentenceNodeHandler(),
            fakeNotificationHandler = FakeNotificationNodeHandler(),
            visualNovelHandler = VisualNovelNodeHandler(textProcessor)
        ),
        nodeResolver = NodeResolver(),
        memory = memory,
        timingScheduler = FakeTimingScheduler(),
        textProcessor = textProcessor,
        characterRepository = characterRepository
    )
}

internal fun startOnlyGraph(): ChapterGraph = ChapterGraph(
    chapterCode = "1A",
    title = "Test",
    nodes = mapOf("start" to Node.Start(id = "start")),
    edges = emptyList(),
    startNodeId = "start"
)

internal fun choiceGraph(
    choiceAText: String = "Option A",
    choiceBText: String = "Option B"
): ChapterGraph = ChapterGraph(
    chapterCode = "1A",
    title = "Test",
    nodes = mapOf(
        "start" to Node.Start(id = "start"),
        "choiceA" to Node.Message(id = "choiceA", text = choiceAText, characterId = 1),
        "choiceB" to Node.Message(id = "choiceB", text = choiceBText, characterId = 1)
    ),
    edges = listOf(
        Edge(source = "start", target = "choiceA", type = EdgeType.NORMAL),
        Edge(source = "start", target = "choiceB", type = EdgeType.NORMAL)
    ),
    startNodeId = "start"
)

internal fun messageGraph(
    text: String,
    isHesitating: Boolean = false
): ChapterGraph = ChapterGraph(
    chapterCode = "1A",
    title = "Test",
    nodes = mapOf(
        "start" to Node.Start(id = "start"),
        "msg" to Node.Message(id = "msg", text = text, characterId = 1, isHesitating = isHesitating),
        "end" to Node.End(id = "end")
    ),
    edges = listOf(
        Edge(source = "start", target = "msg", type = EdgeType.NORMAL),
        Edge(source = "msg", target = "end", type = EdgeType.NORMAL)
    ),
    startNodeId = "start"
)
