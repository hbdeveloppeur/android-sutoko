package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handler for chapter change nodes.
 *
 * Emits a ChangeChapter effect to notify the presentation layer
 * that the chapter should transition. The actual chapter navigation
 * is handled by the engine's NodeResolver.
 */
class ChapterChangeNodeHandler @Inject constructor() : NodeHandler {
    override fun prepare(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val chapterNode = node as? Node.ChapterChange ?: return HandlerScript()

        return HandlerScript(
            commands = listOf(
                HandlerCommand.Emit(HandlerEffect.ChangeChapter(chapterNode.chapterCode))
            )
        )
    }
}
