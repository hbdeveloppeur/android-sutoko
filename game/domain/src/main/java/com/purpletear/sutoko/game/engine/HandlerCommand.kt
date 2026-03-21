package com.purpletear.sutoko.game.engine

/**
 * Commands returned by NodeHandlers that the GameEngine executes.
 *
 * This command pattern separates WHAT should happen (domain logic in handlers)
 * from WHEN it happens (execution control in the engine). This enables:
 * - Immediate effect emission (no batching delays)
 * - Resume-capable execution (each step can be interrupted and resumed)
 * - Testable handlers (pure functions, no suspend)
 * - Clear control flow (explicit step-by-step execution)
 *
 * @see NodeHandler.prepare
 * @see GameEngine.executeNode
 */
sealed class HandlerCommand {

    /**
     * Emit an effect immediately. The engine applies this effect
     * synchronously before proceeding to the next command.
     */
    data class Emit(val effect: HandlerEffect) : HandlerCommand()

    /**
     * Pause execution for the specified duration.
     * The engine handles the actual delay via TimingScheduler.
     *
     * @param millis Duration in milliseconds. Must be >= 0.
     */
    data class Delay(val millis: Long) : HandlerCommand() {
        init {
            require(millis >= 0) { "Delay must be non-negative, was $millis" }
        }
    }

    /**
     * Pause execution and wait for player input.
     * The engine transitions to AwaitingInput state.
     *
     * @param choices Available choices for the player
     */
    data class AwaitInput(val choices: List<HandlerEffect.ShowChoices.Choice>) : HandlerCommand()
}
