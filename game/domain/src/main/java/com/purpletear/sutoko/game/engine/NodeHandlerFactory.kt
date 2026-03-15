package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.engine.handlers.BackgroundNodeHandler
import com.purpletear.sutoko.game.engine.handlers.ChapterChangeNodeHandler
import com.purpletear.sutoko.game.engine.handlers.ConditionNodeHandler
import com.purpletear.sutoko.game.engine.handlers.InfoNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MemoryNodeHandler
import com.purpletear.sutoko.game.engine.handlers.MessageNodeHandler
import com.purpletear.sutoko.game.engine.handlers.SignalNodeHandler
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
    private val chapterChangeHandler: ChapterChangeNodeHandler,
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
        NodeType.CONDITION -> conditionHandler
        NodeType.MEMORY -> memoryHandler
        NodeType.INFO -> infoHandler
        NodeType.TROPHY -> trophyHandler
        NodeType.SIGNAL -> signalHandler
        NodeType.BACKGROUND -> backgroundHandler
    }
}
