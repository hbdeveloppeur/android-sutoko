package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.engine.message.GameMessageTyping
import com.purpletear.sutoko.game.engine.processing.TextProcessor
import com.purpletear.sutoko.game.model.chapter.ConversationMode
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
 *
 * Respects conversation mode from [ConversationModeChangeNodeHandler]:
 * - SMS mode: Shows typing indicators with delays (default)
 * - IRL mode: No typing, messages display immediately
 */
class MessageNodeHandler @Inject constructor(
    private val textProcessor: TextProcessor,
) : NodeHandler {

    override fun buildScript(
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
                    commands = buildMessageCommands(
                        messageNode,
                        processedText,
                        memory.conversationMode
                    )
                )
            }
        }
    }

    /**
     * Builds the command sequence for a message.
     *
     * SMS mode (default):
     * 1. Delay(seenMs) - wait before showing typing
     * 2. Emit(AddMessage) - show typing indicator
     * 3. Delay(waitMs) - typing duration
     *
     * IRL mode:
     * 1. Emit(AddMessage) - immediate display, no typing delays
     *
     * Each effect is applied immediately by the engine.
     */
    private fun buildMessageCommands(
        node: Node.Message,
        processedText: String,
        mode: ConversationMode
    ): List<HandlerCommand> {
        val commands = mutableListOf<HandlerCommand>()
        val messageId = UUID.randomUUID().toString()

        when (mode) {
            ConversationMode.SMS -> {
                commands.add(HandlerCommand.Delay(node.seenMs.coerceAtLeast(520)))

                commands.add(
                    HandlerCommand.Emit(
                        HandlerEffect.AddMessage(
                            GameMessageTyping(
                                id = messageId,
                                characterId = node.characterId,
                            )
                        )
                    )
                )

                val typingDelayMs = determineTypingDuration(node, processedText)
                commands.add(HandlerCommand.Delay(typingDelayMs))

                commands.add(
                    HandlerCommand.Emit(
                        HandlerEffect.DeleteMessage(messageId = messageId)
                    )
                )

                commands.add(HandlerCommand.Delay(node.seenMs.coerceAtLeast(280)))

                commands.add(
                    HandlerCommand.Emit(
                        HandlerEffect.AddMessage(
                            GameMessageText(
                                id = messageId,
                                text = processedText,
                                characterId = node.characterId,
                            )
                        )
                    )
                )
            }

            ConversationMode.IRL -> {
                if (node.seenMs > 0) {
                    commands.add(HandlerCommand.Delay(node.seenMs))
                }

                commands.add(
                    HandlerCommand.Emit(
                        HandlerEffect.AddMessage(
                            GameMessageText(
                                id = messageId,
                                text = processedText,
                                characterId = node.characterId,
                            )
                        )
                    )
                )
            }
        }

        return commands
    }

    private fun determineTypingDuration(node: Node.Message, text: String): Long {
        return if (node.waitMs.toInt() == 0) {
            val baseDuration = text.length * 100L
            baseDuration.coerceIn(1500L, 5000L)
        } else {
            node.waitMs
        }
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
