package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.engine.message.GameMessageInfo
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import java.util.UUID
import javax.inject.Inject

/**
 * Handler for info nodes.
 *
 * Info nodes are silent nodes used for comments or metadata.
 * They produce no commands and simply pass through to the next node.
 */
class InfoNodeHandler @Inject constructor() : NodeHandler {
    override fun buildScript(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val messageNode = node as? Node.Info ?: return HandlerScript()

        return HandlerScript(
            commands = buildMessageCommands(
                messageNode,
            )
        )
    }

    private fun buildMessageCommands(
        node: Node.Info,
    ): List<HandlerCommand> {
        val commands = mutableListOf<HandlerCommand>()
        val messageId = UUID.randomUUID().toString()

        commands.add(HandlerCommand.Delay(node.seenMs.coerceAtLeast(280)))

        commands.add(
            HandlerCommand.Emit(
                HandlerEffect.AddMessage(
                    GameMessageInfo(
                        id = messageId,
                        text = node.text,
                    )
                )
            )
        )

        return commands
    }
}
