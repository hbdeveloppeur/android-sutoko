package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.engine.message.GameMessageVocal
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import java.util.UUID
import javax.inject.Inject

/**
 * Handler for message-vocal nodes.
 *
 * Emits an AddMessage effect with a GameMessageVocal for UI display,
 * followed by a PlayVocal effect to play the audio.
 */
class MessageVocalNodeHandler @Inject constructor() : NodeHandler {
    override fun buildScript(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val vocalNode = node as? Node.MessageVocal ?: return HandlerScript()

        val commands = mutableListOf<HandlerCommand>()

        commands.add(
            HandlerCommand.Emit(
                HandlerEffect.AddMessage(
                    GameMessageVocal(
                        id = UUID.randomUUID().toString(),
                        audioUrl = vocalNode.audioUrl,
                        characterId = vocalNode.characterId,
                    )
                )
            )
        )

        return HandlerScript(commands = commands)
    }
}
