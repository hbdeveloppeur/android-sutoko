package com.purpletear.sutoko.game.engine.timing

/**
 * Abstraction for time-based operations in the game engine.
 * Allows for testable timing without depending on framework coroutines.
 */
interface TimingScheduler {
    /**
     * Suspends execution for the specified duration.
     * @param millis Duration in milliseconds
     */
    suspend fun delay(millis: Long)

    /**
     * Returns the current time in milliseconds.
     * Useful for calculating elapsed time.
     */
    fun now(): Long
}
