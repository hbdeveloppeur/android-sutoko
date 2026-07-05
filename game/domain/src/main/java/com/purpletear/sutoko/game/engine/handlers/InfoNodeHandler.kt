package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.GameEngineLogger
import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.engine.message.GameMessageInfo
import com.purpletear.sutoko.game.engine.processing.TextProcessor
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import java.util.UUID
import javax.inject.Inject

/**
 * Handler for info nodes.
 *
 * Info nodes display narration text centered on screen.
 * Text variables (e.g. [prenom]) are resolved using [TextProcessor]
 * before the message is emitted.
 */
class InfoNodeHandler @Inject constructor(
    private val textProcessor: TextProcessor,
) : NodeHandler {
    override fun buildScript(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val messageNode = node as? Node.Info ?: return HandlerScript()

        val variables = memory.state.value
        val processedText = textProcessor.process(messageNode.text, variables)

        GameEngineLogger.d("HAND") {
            "Info node ${messageNode.id}: raw=\"${messageNode.text}\" processed=\"$processedText\""
        }

        return HandlerScript(
            commands = buildMessageCommands(
                node = messageNode,
                processedText = processedText,
            )
        )
    }

    private fun buildMessageCommands(
        node: Node.Info,
        processedText: String,
    ): List<HandlerCommand> {
        val commands = mutableListOf<HandlerCommand>()
        val messageId = UUID.randomUUID().toString()

        commands.add(HandlerCommand.Delay(2000))

        commands.add(
            HandlerCommand.Emit(
                HandlerEffect.AddMessage(
                    GameMessageInfo(
                        id = messageId,
                        text = processedText,
                    )
                )
            )
        )

        return commands
    }
}
