package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.engine.message.GameMessageImage
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import java.util.UUID
import javax.inject.Inject

/**
 * Handler for message-image nodes.
 *
 * Handles the image message execution sequence with timing:
 * 1. seenMs delay - wait before showing typing
 * 2. Add message with TYPING status (shows typing indicator)
 * 3. waitMs delay - typing duration
 * 4. Update message status to Image (shows image, hides typing)
 *
 * Respects conversation mode:
 * - SMS mode: Shows typing indicators with delays (default)
 * - IRL mode: No typing, images display immediately
 */
class MessageImageNodeHandler @Inject constructor() : NodeHandler {

    override fun buildScript(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val imageNode = node as? Node.MessageImage ?: return HandlerScript()

        return HandlerScript(
            commands = buildImageCommands(
                imageNode,
            )
        )
    }

    /**
     * Builds the command sequence for an image message.
     *
     * SMS mode (default):
     * 1. Delay(seenMs) - wait before showing typing
     * 2. Emit(AddMessage) - show typing indicator
     * 3. Delay(waitMs) - typing duration
     * 4. Delete typing, add image message
     *
     * IRL mode:
     * 1. Emit(AddMessage) - immediate display, no typing delays
     */
    private fun buildImageCommands(
        node: Node.MessageImage,
    ): List<HandlerCommand> {
        val commands = mutableListOf<HandlerCommand>()
        val messageId = UUID.randomUUID().toString()

        commands.add(HandlerCommand.Delay(node.seenMs.coerceAtLeast(520)))

        commands.add(
            HandlerCommand.Emit(
                HandlerEffect.AddMessage(
                    GameMessageImage(
                        id = messageId,
                        imageUrl = node.imageUrl,
                        characterId = node.characterId,
                    )
                )
            )
        )

        return commands
    }
}
