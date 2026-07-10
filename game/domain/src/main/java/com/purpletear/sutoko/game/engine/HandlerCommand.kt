package com.purpletear.sutoko.game.engine

import androidx.annotation.Keep

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
    @Keep
    data class Emit(val effect: HandlerEffect) : HandlerCommand()

    /**
     * Pause execution for the specified duration.
     * The engine handles the actual delay via TimingScheduler.
     *
     * @param millis Duration in milliseconds. Must be >= 0.
     */
    @Keep
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
    @Keep
    data class AwaitInput(val choices: List<HandlerEffect.ShowChoices.Choice>) : HandlerCommand()

    /**
     * Halt execution and park the engine until the player dismisses the manga page.
     *
     * Contract:
     * - MUST be the last command in the script (enforced by the engine, like [AwaitInput]).
     * - Halts the script: no further command runs and the engine does NOT navigate to the next node.
     * - The engine transitions to [GameEngineState.AwaitingMangaDismissal] and stays parked until
     *   [GameEngine.resumeFromMangaPage] is called (triggered by the page dismiss), which then
     *   resolves the next node via the normal navigation path.
     *
     * Carries no payload: the page content is the [com.purpletear.sutoko.game.engine.message.GameMessageMangaPage]
     * already emitted by a preceding [Emit] of [HandlerEffect.AddMessage].
     */
    @Keep
    data object AwaitMangaDismissal : HandlerCommand()
}
