package com.purpletear.sutoko.game.engine.handlers

import com.purpletear.sutoko.game.engine.GameEngineLogger
import com.purpletear.sutoko.game.engine.HandlerCommand
import com.purpletear.sutoko.game.engine.HandlerEffect
import com.purpletear.sutoko.game.engine.HandlerScript
import com.purpletear.sutoko.game.engine.NodeHandler
import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import javax.inject.Inject

/**
 * Handler for message theme nodes.
 *
 * A theme node is a styling directive, not content: it does not emit a message and does not pause.
 * It records the active bubble background and text foreground colors in [GameMemory] so that
 * [MessageNodeHandler] can stamp them onto each subsequent message until the next theme node.
 *
 * Each channel is written only when provided: a null field leaves the previously stored value
 * unchanged, so a partial theme overrides only the channels it specifies.
 */
class MessageThemeNodeHandler @Inject constructor() : NodeHandler {

    override fun buildScript(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val themeNode = node as? Node.MessageTheme ?: return HandlerScript()

        GameEngineLogger.d("HAND") {
            "Message theme ${themeNode.id} → bg=${themeNode.backgroundColor} fg=${themeNode.foregroundColor}"
        }

        val commands = buildList {
            themeNode.backgroundColor?.let {
                add(HandlerCommand.Emit(HandlerEffect.UpdateMemory(GameMemory.MESSAGE_THEME_BG_KEY, it)))
            }
            themeNode.foregroundColor?.let {
                add(HandlerCommand.Emit(HandlerEffect.UpdateMemory(GameMemory.MESSAGE_THEME_FG_KEY, it)))
            }
        }

        return HandlerScript(commands = commands)
    }
}
