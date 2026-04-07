package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handler for conversation mode change nodes.
 *
 * Changes the display mode between SMS (chat with typing) and IRL (direct dialogue).
 * Updates both memory (for handlers to read) and notifies UI via effect.
 *
 * The mode is stored in memory with key [CONVERSATION_MODE_KEY] so that
 * [MessageNodeHandler] can access it when building message scripts.
 */
class ConversationModeChangeNodeHandler @Inject constructor() : NodeHandler {

    override fun buildScript(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val modeNode = node as? Node.ConversationModeChange ?: return HandlerScript()

        val modeName = modeNode.mode.name

        return HandlerScript(
            commands = listOf(
                // Update memory so handlers can read current mode
                HandlerCommand.Emit(
                    HandlerEffect.UpdateMemory(GameMemory.CONVERSATION_MODE_KEY, modeName)
                ),
                // Notify UI to update rendering style
                HandlerCommand.Emit(
                    HandlerEffect.ChangeConversationMode(modeName)
                )
            )
        )
    }
}
