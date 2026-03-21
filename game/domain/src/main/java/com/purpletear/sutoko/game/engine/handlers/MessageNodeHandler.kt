package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler

import com.purpletear.sutoko.game.engine.processing.TextProcessor
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import java.util.UUID
import javax.inject.Inject

/**
 * Handler for message nodes.
 *
 * Handles the full message execution sequence with timing:
 * 1. seenMs delay - wait before showing typing
 * 2. Add message with TYPING status (shows typing indicator)
 * 3. waitMs delay - typing duration
 * 4. Update message status to SENT (shows message, hides typing)
 *
 * Also handles special commands embedded in messages:
 * - [BACKGROUND_<url>] - Change background image
 * - [<command>] - Skip/ignore commands
 */
class MessageNodeHandler @Inject constructor(
    private val textProcessor: TextProcessor
) : NodeHandler {

    override fun prepare(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val messageNode = node as? Node.Message ?: return HandlerScript()

        val variables = memory.state.value
        val processedText = textProcessor.process(messageNode.text, variables)
        val command = parseCommand(processedText)

        return when (command) {
            is Command.ChangeBackground -> {
                HandlerScript(
                    commands = listOf(
                        HandlerCommand.Emit(HandlerEffect.ChangeBackground(command.imageUrl))
                    )
                )
            }

            is Command.Skip -> HandlerScript()

            is Command.Message -> {
                HandlerScript(
                    commands = buildMessageCommands(messageNode, processedText)
                )
            }
        }
    }

    /**
     * Builds the command sequence for a timed message:
     * 1. Delay(seenMs) - wait before showing typing
     * 2. Emit(AddMessage with TYPING) - show typing indicator
     * 3. Delay(waitMs) - typing duration
     * 4. Emit(UpdateLastMessageStatus to SENT) - reveal message
     *
     * Each effect is applied immediately by the engine, ensuring the UI
     * sees the TYPING state before the delay and SENT state after.
     */
    private fun buildMessageCommands(
        node: Node.Message,
        processedText: String
    ): List<HandlerCommand> {
        val commands = mutableListOf<HandlerCommand>()
        val messageId = UUID.randomUUID().toString()

        // 1. SEEN DELAY: Wait before showing typing (simulates "reading" previous message)
        if (node.seenMs > 0) {
            commands.add(HandlerCommand.Delay(node.seenMs))
        }

        // 2. SHOW TYPING: Add message with TYPING status (triggers typing indicator in UI)
        commands.add(
            HandlerCommand.Emit(
                HandlerEffect.AddMessage(
                    GameMessage(
                        id = messageId,
                        text = processedText,
                        characterId = node.characterId,
                        status = GameMessage.Status.TYPING
                    )
                )
            )
        )

        // 3. TYPING DELAY: Wait during "typing"
        if (node.waitMs > 0) {
            commands.add(HandlerCommand.Delay(node.waitMs))
        }

        // 4. REVEAL: Update to SENT status (reveals the message, hides typing indicator)
        commands.add(
            HandlerCommand.Emit(
                HandlerEffect.UpdateLastMessageStatus(GameMessage.Status.SENT)
            )
        )

        return commands
    }

    private fun parseCommand(text: String): Command {
        return when {
            text.startsWith("[BACKGROUND_") && text.endsWith("]") -> {
                Command.ChangeBackground(
                    text.removePrefix("[BACKGROUND_").removeSuffix("]")
                )
            }

            text.startsWith("[") && text.endsWith("]") -> Command.Skip
            else -> Command.Message
        }
    }

    private sealed class Command {
        data class ChangeBackground(val imageUrl: String) : Command()
        data object Skip : Command()
        data object Message : Command()
    }
}
