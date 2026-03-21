package com.purpletear.sutoko.game.engine

import com.purpletear.sutoko.game.model.chapter.GameMemory
import com.purpletear.sutoko.game.model.chapter.Node

/**
 * Handler for executing a specific node type.
 * Implementations are wired via NodeHandlerFactory.
 *
 * A handler is pure logic: given a Node and GameMemory, it returns a script
 * of commands that the GameEngine executes. The handler does NOT:
 * - Suspend or block (returns immediately)
 * - Mutate GameMemory (use UpdateMemory effect)
 * - Touch UI or Android framework
 *
 * Key principles:
 * - Handlers are synchronous pure functions (testable, predictable)
 * - Temporal concerns (delays) are expressed as Delay commands
 * - The engine controls execution timing and can interrupt/resume
 * - All side effects are explicit HandlerEffect values
 */
interface NodeHandler {
    /**
     * Prepares a script of commands for the engine to execute.
     *
     * @param node The node to execute
     * @param memory The game memory for read-only state inspection
     * @return HandlerScript containing:
     *         - commands: sequence of Emit/Delay/AwaitInput to execute
     *         - nextNodeId: explicit next node, or null to resolve via edges
     */
    fun prepare(
        node: Node,
        memory: GameMemory
    ): HandlerScript
}

/**
 * Result returned by a NodeHandler containing the execution script.
 *
 * @property commands Ordered list of commands for the engine to execute.
 *                    Effects are applied immediately when encountered.
 *                    Delays are handled by the engine's TimingScheduler.
 * @property nextNodeId Explicit next node ID, or null to let engine resolve via graph edges
 */
data class HandlerScript(
    val commands: List<HandlerCommand> = emptyList(),
    val nextNodeId: String? = null
)
