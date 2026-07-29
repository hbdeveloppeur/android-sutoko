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
 * Handler for fake-notification nodes.
 *
 * Emits a ShowFakeNotification effect so the presentation layer displays a decorative,
 * non-clickable notification overlay. The engine waits [Node.FakeNotification.delayMs]
 * before showing it and [Node.FakeNotification.durationMs] while it is on screen, then
 * continues automatically (unlike the legacy app, no tap is required to resume).
 */
class FakeNotificationNodeHandler @Inject constructor() : NodeHandler {
    override fun buildScript(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val notification = node as? Node.FakeNotification ?: return HandlerScript()

        GameEngineLogger.d("HAND") { "Fake notification ${notification.id} (delay=${notification.delayMs}ms, duration=${notification.durationMs}ms)" }

        return HandlerScript(
            commands = buildList {
                if (notification.delayMs > 0) {
                    add(HandlerCommand.Delay(notification.delayMs))
                }
                add(
                    HandlerCommand.Emit(
                        HandlerEffect.ShowFakeNotification(
                            title = notification.title,
                            subtitle = notification.subtitle,
                            actionText = notification.actionText,
                            imageUrl = notification.imageUrl,
                            characterId = notification.characterId,
                            durationMs = notification.durationMs
                        )
                    )
                )
                if (notification.durationMs > 0) {
                    add(HandlerCommand.Delay(notification.durationMs))
                }
            }
        )
    }
}
