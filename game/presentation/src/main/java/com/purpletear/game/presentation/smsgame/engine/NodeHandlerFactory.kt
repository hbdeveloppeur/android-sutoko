package com.purpletear.game.presentation.smsgame.engine

import com.purpletear.game.presentation.smsgame.engine.handlers.BackgroundNodeHandler
import com.purpletear.game.presentation.smsgame.engine.handlers.ChapterChangeNodeHandler
import com.purpletear.game.presentation.smsgame.engine.handlers.ChoiceNodeHandler
import com.purpletear.game.presentation.smsgame.engine.handlers.ConditionNodeHandler
import com.purpletear.game.presentation.smsgame.engine.handlers.InfoNodeHandler
import com.purpletear.game.presentation.smsgame.engine.handlers.MemoryNodeHandler
import com.purpletear.game.presentation.smsgame.engine.handlers.MessageNodeHandler
import com.purpletear.game.presentation.smsgame.engine.handlers.SignalNodeHandler
import com.purpletear.game.presentation.smsgame.engine.handlers.StartNodeHandler
import com.purpletear.game.presentation.smsgame.engine.handlers.TrophyNodeHandler
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
    private val chapterChangeHandler: ChapterChangeNodeHandler,
    private val choiceHandler: ChoiceNodeHandler,
    private val conditionHandler: ConditionNodeHandler,
    private val memoryHandler: MemoryNodeHandler,
    private val infoHandler: InfoNodeHandler,
    private val trophyHandler: TrophyNodeHandler,
    private val signalHandler: SignalNodeHandler,
    private val backgroundHandler: BackgroundNodeHandler
) {
    /**
     * Returns the handler for the given node type.
     * Compile-time exhaustive - adding a new NodeType requires updating this when.
     */
    fun getHandler(type: NodeType): NodeHandler = when (type) {
        NodeType.START -> startHandler
        NodeType.MESSAGE -> messageHandler
        NodeType.CHAPTER_CHANGE -> chapterChangeHandler
        NodeType.CHOICE -> choiceHandler
        NodeType.CONDITION -> conditionHandler
        NodeType.MEMORY -> memoryHandler
        NodeType.INFO -> infoHandler
        NodeType.TROPHY -> trophyHandler
        NodeType.SIGNAL -> signalHandler
        NodeType.BACKGROUND -> backgroundHandler
    }
}
