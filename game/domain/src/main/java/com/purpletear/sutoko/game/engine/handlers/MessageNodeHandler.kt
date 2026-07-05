package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.GameEngineLogger
import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.PreviousNodeAwareNodeHandler
import com.purpletear.sutoko.game.engine.message.GameMessageText
import com.purpletear.sutoko.game.engine.message.GameMessageTyping
import com.purpletear.sutoko.game.engine.processing.TextProcessor
import com.purpletear.sutoko.game.model.chapter.ConversationMode
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

/**
 * Handler for message nodes.
 *
 * Builds the execution script for a message, with behavior depending on the current
 * [ConversationMode]:
 * - SMS mode: typing indicator, optional hesitation, and delays.
 * - IRL mode: immediate display with optional timing delay.
 *
 * Text variables (e.g. [prenom]) are resolved using [TextProcessor] before the
 * message is emitted. Bracketed text that remains after substitution is treated as
 * a skip command and produces no message.
 *
 * The handler is [PreviousNodeAwareNodeHandler] because the first message from the
 * main character must not have an initial seen delay.
 */
class MessageNodeHandler @Inject constructor(
    private val textProcessor: TextProcessor,
) : PreviousNodeAwareNodeHandler {

    override fun buildScript(node: Node, memory: GameMemory): HandlerScript {
        return buildScript(node, memory, previousNode = null)
    }

    override fun buildScript(
        node: Node,
        memory: GameMemory,
        previousNode: Node?
    ): HandlerScript {
        val messageNode = node as? Node.Message ?: return HandlerScript()

        val variables = memory.state.value
        val processedText = textProcessor.process(messageNode.text, variables)
        val command = parseCommand(processedText)

        GameEngineLogger.d("HAND") {
            "Message ${messageNode.id}: raw=\"${messageNode.text}\" processed=\"$processedText\" mode=${memory.conversationMode}"
        }

        return when (command) {
            is Command.Skip -> {
                GameEngineLogger.d("HAND") { "Skip command: $processedText" }
                HandlerScript()
            }

            is Command.Message -> buildMessageScript(
                messageNode,
                processedText,
                memory.conversationMode,
                previousNode,
                memory,
            )
        }
    }

    private fun buildMessageScript(
        node: Node.Message,
        processedText: String,
        mode: ConversationMode,
        previousNode: Node?,
        memory: GameMemory,
    ): HandlerScript {
        if (processedText.isBlank()) {
            GameEngineLogger.d("MSG") {
                "Skipping blank message ${node.id} from character ${node.characterId}"
            }
            return HandlerScript()
        }

        GameEngineLogger.d("MSG") {
            "MessageText from character ${node.characterId}: \"$processedText\""
        }

        val messageId = UUID.randomUUID().toString()
        val commands = when (mode) {
            ConversationMode.SMS -> buildSmsScript(
                node,
                processedText,
                messageId,
                previousNode,
                memory
            )

            ConversationMode.IRL -> buildIrlScript(
                node,
                processedText,
                messageId,
                previousNode,
                memory
            )
        }

        return HandlerScript(commands = commands)
    }

    private fun buildSmsScript(
        node: Node.Message,
        text: String,
        messageId: String,
        previousNode: Node?,
        memory: GameMemory,
    ): List<HandlerCommand> {
        val commands = mutableListOf<HandlerCommand>()

        if (node.isHesitating) {
            addHesitationScript(commands, messageId, node.characterId)
        }

        commands.add(HandlerCommand.Emit(HandlerEffect.PlayTypingSound))
        commands.add(emitAddTyping(messageId, node.characterId))

        val typingDelayMs = determineTypingDuration(node, text)
        commands.add(HandlerCommand.Delay(typingDelayMs))

        commands.add(HandlerCommand.Emit(HandlerEffect.DeleteMessage(messageId = messageId)))
        commands.add(HandlerCommand.Delay(node.seenMs.coerceAtLeast(MIN_POST_TYPING_DELAY_MS)))
        commands.add(emitAddText(text, messageId, node.characterId))

        return commands
    }

    private fun buildIrlScript(
        node: Node.Message,
        text: String,
        messageId: String,
        previousNode: Node?,
        memory: GameMemory,
    ): List<HandlerCommand> {
        val commands = mutableListOf<HandlerCommand>()

        if (!memory.isMainCharacter(node.characterId)) {
            when {
                node.isAutoTiming -> {
                    val text = if (previousNode is Node.Message) {
                        previousNode.text
                    } else if (previousNode is Node.Info) {
                        previousNode.text
                    } else {
                        null
                    }
                    text?.let {
                        commands.add(HandlerCommand.Delay(determineReadingDuration(it)))
                    } ?: {
                        commands.add(HandlerCommand.Delay(IRL_AUTO_TIMING_DELAY_MS))
                    }
                }

                node.seenMs > 0 -> commands.add(HandlerCommand.Delay(node.seenMs))
            }
        }


        commands.add(emitAddText(text, messageId, node.characterId))

        return commands
    }

    private fun addHesitationScript(
        commands: MutableList<HandlerCommand>,
        messageId: String,
        characterId: Int
    ) {
        commands.add(emitAddTyping(messageId, characterId))
        commands.add(HandlerCommand.Emit(HandlerEffect.PlayTypingSound))
        commands.add(
            HandlerCommand.Delay(
                Random.nextLong(
                    HESITATION_DELAY_MIN_MS,
                    HESITATION_DELAY_MAX_EXCLUSIVE_MS
                )
            )
        )
        commands.add(HandlerCommand.Emit(HandlerEffect.DeleteMessage(messageId = messageId)))
        commands.add(
            HandlerCommand.Delay(
                Random.nextLong(
                    HESITATION_DELAY_MIN_MS,
                    HESITATION_DELAY_MAX_EXCLUSIVE_MS
                )
            )
        )
    }

    private fun emitAddTyping(messageId: String, characterId: Int): HandlerCommand =
        HandlerCommand.Emit(
            HandlerEffect.AddMessage(
                GameMessageTyping(
                    id = messageId,
                    characterId = characterId,
                )
            )
        )

    private fun emitAddText(text: String, messageId: String, characterId: Int): HandlerCommand =
        HandlerCommand.Emit(
            HandlerEffect.AddMessage(
                GameMessageText(
                    id = messageId,
                    text = text,
                    characterId = characterId,
                )
            )
        )

    private fun determineTypingDuration(node: Node.Message, text: String): Long {
        return if (node.waitMs == 0L) {
            val baseDuration = text.length * TYPING_CHAR_DELAY_MS
            baseDuration.coerceIn(MIN_TYPING_DURATION_MS, MAX_TYPING_DURATION_MS)
        } else {
            node.waitMs
        }
    }


    private fun determineReadingDuration(text: String): Long {
        val baseDuration = text.length * READING_CHAR_DELAY_MS
        return baseDuration.coerceIn(MIN_TYPING_DURATION_MS, MAX_TYPING_DURATION_MS)
    }

    private fun parseCommand(text: String): Command {
        return when {
            text.startsWith("[") && text.endsWith("]") -> Command.Skip
            else -> Command.Message
        }
    }

    private sealed class Command {
        data object Skip : Command()
        data object Message : Command()
    }

    private companion object {
        private const val MIN_SEEN_DELAY_MS = 520L
        private const val MIN_POST_TYPING_DELAY_MS = 280L
        private const val IRL_AUTO_TIMING_DELAY_MS = 2000L
        private const val HESITATION_DELAY_MIN_MS = 1000L
        private const val HESITATION_DELAY_MAX_EXCLUSIVE_MS = 3001L
        private const val TYPING_CHAR_DELAY_MS = 100L
        private const val READING_CHAR_DELAY_MS = 250L
        private const val MIN_TYPING_DURATION_MS = 1500L
        private const val MAX_TYPING_DURATION_MS = 5000L
    }
}
