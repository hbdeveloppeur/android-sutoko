package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.GameEvent
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.engine.processing.TextProcessor
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handler for message nodes.
 * 
 * NOTE: For standard message execution, timing is orchestrated by GameEngine.
 * This handler is used for:
 * 1. Special message handling scenarios (commands, background changes)
 * 2. Text processing via TextProcessor
 */
class MessageNodeHandler @Inject constructor(
    private val textProcessor: TextProcessor
) : NodeHandler {
    override suspend fun handle(
        node: Node,
        memory: GameMemory,
        emit: (GameEvent) -> Unit,
    ): String? {
        val messageNode = node as? Node.Message ?: return null

        val variables = memory.state.value
        val processedText = textProcessor.process(messageNode.text, variables)
        val command = parseCommand(processedText)

        return when (command) {
            is Command.ChangeBackground -> {
                emit(GameEvent.ChangeBackground(command.imageUrl))
                null
            }

            is Command.Skip -> null
            is Command.Message -> {
                emit(
                    GameEvent.ShowMessage(
                        text = processedText,
                        characterId = messageNode.characterId,
                        isMainCharacter = messageNode.characterId == 0
                    )
                )
                null
            }
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
