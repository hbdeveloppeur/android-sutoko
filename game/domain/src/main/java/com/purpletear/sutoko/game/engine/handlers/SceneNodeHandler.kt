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
 * Handler for scene nodes.
 *
 * Emits a ChangeScene effect to transition to a new scene configuration.
 * The presentation layer resolves the sceneId to the actual image/video/color
 * using the SceneRepository.
 */
class SceneNodeHandler @Inject constructor() : NodeHandler {
    override fun buildScript(
        node: Node,
        memory: GameMemory
    ): HandlerScript {
        val sceneNode = node as? Node.Scene ?: return HandlerScript()

        GameEngineLogger.d("HAND") { "Scene change ${sceneNode.id} → ${sceneNode.sceneId}" }

        return HandlerScript(
            commands = listOf(
                HandlerCommand.Emit(
                    HandlerEffect.ChangeScene(
                        sceneId = sceneNode.sceneId
                    ),
                ),
                // Scene changes are atmospheric: they must auto-appear and continue even
                // in click-to-advance mode, like sound nodes which carry no tap gate at all.
                HandlerCommand.AwaitTap(SCENE_VIEW_DURATION_MS, requiresTap = false),
            )
        )
    }

    private companion object {
        private const val SCENE_VIEW_DURATION_MS = 3000L
    }
}
