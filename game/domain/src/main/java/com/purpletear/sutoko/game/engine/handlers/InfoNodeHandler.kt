package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.GameEngineLogger
import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.ArrivalContext
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.PreviousNodeAwareNodeHandler
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
) : PreviousNodeAwareNodeHandler {

    override fun buildScript(node: Node, memory: GameMemory): HandlerScript {
        return buildScript(node, memory, previousNode = null, arrivalContext = ArrivalContext())
    }

    override fun buildScript(
        node: Node,
        memory: GameMemory,
        previousNode: Node?,
        arrivalContext: ArrivalContext,
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
                previousNode = previousNode,
            )
        )
    }

    private fun buildMessageCommands(
        node: Node.Info,
        processedText: String,
        previousNode: Node?,
    ): List<HandlerCommand> {
        val messageId = UUID.randomUUID().toString()

        return listOf(
            HandlerCommand.Emit(
                HandlerEffect.AddMessage(
                    GameMessageInfo(
                        id = messageId,
                        text = processedText,
                    )
                )
            ),
            HandlerCommand.AwaitTap,
        )
    }
}
