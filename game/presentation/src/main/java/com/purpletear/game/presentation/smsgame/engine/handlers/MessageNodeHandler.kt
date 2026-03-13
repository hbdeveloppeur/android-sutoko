package com.purpletear.game.presentation.smsgame.engine.handlers

import com.purpletear.game.presentation.smsgame.engine.GameEvent
import com.purpletear.game.presentation.smsgame.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

class MessageNodeHandler @Inject constructor() : NodeHandler {
    override suspend fun handle(
        node: Node,
        memory: GameMemory,
        emit: (GameEvent) -> Unit
    ): String? {
        val messageNode = node as? Node.Message ?: return null

        val processedText = processText(messageNode.text, memory)
        val command = parseCommand(processedText)

        return when (command) {
            is Command.ChangeBackground -> {
                emit(GameEvent.ChangeBackground(command.imageUrl))
                null
            }
            is Command.UnlockTrophy -> {
                emit(GameEvent.UnlockTrophy(command.trophyId))
                null
            }
            is Command.Skip -> null
            is Command.Message -> {
                emit(
                    GameEvent.ShowMessage(
                        text = processedText,
                        characterId = messageNode.characterId,
                        isMainCharacter = messageNode.characterId == 0,
                        delayMs = messageNode.waitMs
                    )
                )
                null
            }
        }
    }

    private fun processText(text: String, memory: GameMemory): String {
        return text.replace(Regex("\\[prenom\\]")) {
            memory.get("heroName") ?: "Hero"
        }
    }

    private fun parseCommand(text: String): Command {
        return when {
            text.startsWith("[BACKGROUND_") && text.endsWith("]") -> {
                Command.ChangeBackground(
                    text.removePrefix("[BACKGROUND_").removeSuffix("]")
                )
            }
            text.startsWith("[TROPHY$$$") && text.endsWith("]") -> {
                Command.UnlockTrophy(
                    text.removePrefix("[TROPHY$$$").removeSuffix("]")
                )
            }
            text.startsWith("[") && text.endsWith("]") -> Command.Skip
            else -> Command.Message
        }
    }

    private sealed class Command {
        data class ChangeBackground(val imageUrl: String) : Command()
        data class UnlockTrophy(val trophyId: String) : Command()
        data object Skip : Command()
        data object Message : Command()
    }
}
