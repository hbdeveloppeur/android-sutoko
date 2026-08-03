package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.GameEngineLogger
import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.engine.message.GameMessageAudioDialogue
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import java.util.UUID
import javax.inject.Inject

/**
 * Handler for message-audio-dialogue nodes.
 *
 * Emits an AddMessage effect with a GameMessageAudioDialogue for UI display,
 * followed by a PlayVocal effect to play the audio (reuses the vocal playback pipeline).
 */
class MessageAudioDialogueNodeHandler @Inject constructor() : NodeHandler {
    override fun buildScript(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val dialogueNode = node as? Node.MessageAudioDialogue ?: return HandlerScript()

        GameEngineLogger.d("HAND") { "Audio dialogue ${dialogueNode.id}: ${dialogueNode.audioUrl}" }

        val commands = mutableListOf<HandlerCommand>()

        val messageId = UUID.randomUUID().toString()

        commands.add(
            HandlerCommand.Emit(
                HandlerEffect.AddMessage(
                    GameMessageAudioDialogue(
                        id = messageId,
                        audioUrl = dialogueNode.audioUrl,
                        characterId = dialogueNode.characterId,
                        text = dialogueNode.text,
                    )
                )
            )
        )

        commands.add(
            HandlerCommand.Emit(
                HandlerEffect.PlayVocal(dialogueNode.audioUrl)
            )
        )

        return HandlerScript(commands = commands)
    }
}
