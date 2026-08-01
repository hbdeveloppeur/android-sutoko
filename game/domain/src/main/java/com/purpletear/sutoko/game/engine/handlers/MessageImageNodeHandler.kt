package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.GameEngineLogger
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

        GameEngineLogger.d("HAND") { "Image message ${imageNode.id}: ${imageNode.imageUrl}" }

        return HandlerScript(
            commands = buildImageCommands(
                imageNode,
            )
        )
    }

    /**
     * Builds the command sequence for an image message.
     *
     * The image is shown immediately and the engine waits for a tap before continuing.
     */
    private fun buildImageCommands(
        node: Node.MessageImage,
    ): List<HandlerCommand> {
        val messageId = UUID.randomUUID().toString()

        return listOf(
            HandlerCommand.Emit(
                HandlerEffect.AddMessage(
                    GameMessageImage(
                        id = messageId,
                        imageUrl = node.imageUrl,
                        characterId = node.characterId,
                    )
                )
            ),
            HandlerCommand.AwaitTap,
        )
    }
}
