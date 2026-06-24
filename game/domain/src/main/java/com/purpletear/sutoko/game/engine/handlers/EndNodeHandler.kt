package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.GameEngineLogger
import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handler for end nodes.
 *
 * Emits a StoryFinished effect to signal the end of the game/story.
 */
class EndNodeHandler @Inject constructor() : NodeHandler {
    override fun buildScript(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        GameEngineLogger.d("HAND") { "End node ${node.id}: story finished" }
        return HandlerScript(
            commands = listOf(
                HandlerCommand.Emit(HandlerEffect.StoryFinished)
            )
        )
    }
}
