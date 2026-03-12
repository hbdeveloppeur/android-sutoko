package com.purpletear.game.presentation.smsgame.engine

import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node
import dagger.MapKey

/**
 * Handler for executing a specific node type.
 * Implementations are auto-registered via Hilt multibindings.
 */
interface NodeHandler {
    /**
     * Handles the execution of a node.
     *
     * @param node The node to execute
     * @param memory The game memory for state management
     * @param emit Callback to emit UI events
     * @return The next node ID to navigate to, or null to let engine resolve via edges
     */
    suspend fun handle(
        node: Node,
        memory: GameMemory,
        emit: (GameEvent) -> Unit
    ): String?
}

/**
 * Key for mapping node types to handlers in Hilt multibindings.
 */
@MapKey
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class NodeTypeKey(val type: NodeType)
