package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.engine.handlers.BackgroundNodeHandler
import com.purpletear.sutoko.game.engine.handlers.ChapterChangeNodeHandler
import com.purpletear.sutoko.game.engine.handlers.CodeNodeHandler
import com.purpletear.sutoko.game.engine.handlers.ConditionNodeHandler
import com.purpletear.sutoko.game.engine.handlers.ConversationModeChangeNodeHandler
import com.purpletear.sutoko.game.engine.handlers.EndNodeHandler
import com.purpletear.sutoko.game.engine.handlers.InfoNodeHandler
import com.purpletear.sutoko.game.engine.handlers.IntroSentenceNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MemoryNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MessageImageNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MessageNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MessageThemeNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MessageVocalNodeHandler
import com.purpletear.sutoko.game.engine.handlers.SceneNodeHandler
import com.purpletear.sutoko.game.engine.handlers.SoundNodeHandler
import com.purpletear.sutoko.game.engine.handlers.StartNodeHandler
import com.purpletear.sutoko.game.engine.handlers.TrophyNodeHandler
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory for resolving NodeType to NodeHandler.
 * Replaces Hilt multibindings with explicit constructor injection for:
 * - Compile-time safety (exhaustive when expression)
 * - No runtime map allocation
 * - No generated code overhead
 * - Clear dependency graph
 */
@Singleton
class NodeHandlerFactory @Inject constructor(
    private val startHandler: StartNodeHandler,
    private val messageHandler: MessageNodeHandler,
    private val messageThemeHandler: MessageThemeNodeHandler,
    private val messageImageHandler: MessageImageNodeHandler,
    private val chapterChangeHandler: ChapterChangeNodeHandler,
    private val codeHandler: CodeNodeHandler,
    private val conditionHandler: ConditionNodeHandler,
    private val memoryHandler: MemoryNodeHandler,
    private val infoHandler: InfoNodeHandler,
    private val trophyHandler: TrophyNodeHandler,
    private val backgroundHandler: BackgroundNodeHandler,
    private val conversationModeChangeHandler: ConversationModeChangeNodeHandler,
    private val sceneHandler: SceneNodeHandler,
    private val endHandler: EndNodeHandler,
    private val soundHandler: SoundNodeHandler,
    private val messageVocalHandler: MessageVocalNodeHandler,
    private val introSentenceHandler: IntroSentenceNodeHandler,
) {
    /**
     * Returns the handler for the given node type.
     * Compile-time exhaustive - adding a new NodeType requires updating this when.
     */
    fun getHandler(type: NodeType): NodeHandler = when (type) {
        NodeType.START -> startHandler
        NodeType.MESSAGE -> messageHandler
        NodeType.MESSAGE_THEME -> messageThemeHandler
        NodeType.MESSAGE_IMAGE -> messageImageHandler
        NodeType.CHAPTER_CHANGE -> chapterChangeHandler
        NodeType.CONDITION -> conditionHandler
        NodeType.MEMORY -> memoryHandler
        NodeType.INFO -> infoHandler
        NodeType.TROPHY -> trophyHandler
        NodeType.BACKGROUND -> backgroundHandler
        NodeType.CONVERSATION_MODE_CHANGE -> conversationModeChangeHandler
        NodeType.SCENE -> sceneHandler
        NodeType.END -> endHandler
        NodeType.SOUND -> soundHandler
        NodeType.MESSAGE_VOCAL -> messageVocalHandler
        NodeType.CODE -> codeHandler
        NodeType.INTRO_SENTENCE -> introSentenceHandler
    }
}
