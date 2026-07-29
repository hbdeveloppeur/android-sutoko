package com.purpletear.game.data.infrastructure

import com.purpletear.sutoko.game.engine.timing.TimingScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Production implementation of TimingScheduler using Kotlin coroutines.
 *
 * Located in Infrastructure layer as it depends on:
 * - kotlinx.coroutines (external framework)
 * - System.nanoTime() (system resource)
 *
 * This is NOT a Presentation concern - it's a framework/driver implementation detail.
 *
 * Supports a hold-to-pause gate: while [setHoldPaused] is true, any in-flight [delay]
 * freezes (parked suspension, no busy-wait) and resumes with its remaining time once
 * released. Time spent held does not count toward the delay. This is independent from
 * GameEngine.pause()/resume(), which parks the engine at script boundaries.
 */
@Singleton
class SystemTimingScheduler(
    private val nanoTime: () -> Long = System::nanoTime,
) : TimingScheduler {

    // Dagger requires a single @Inject constructor with no default parameters.
    @Inject
    constructor() : this(System::nanoTime)

    private val holdPaused = MutableStateFlow(false)

    /**
     * Holds/releases the timing gate. Idempotent. While held, every current and future
     * [delay] parks until released.
     */
    fun setHoldPaused(paused: Boolean) {
        holdPaused.value = paused
    }

    override suspend fun delay(millis: Long) {
        var remaining = millis
        while (remaining > 0) {
            // Park while the hold gate is closed.
            holdPaused.first { !it }

            val startedAt = nanoTime()
            val held = withTimeoutOrNull(remaining) { holdPaused.first { it } } != null
            if (!held) return

            val elapsedMs = TimeUnit.NANOSECONDS.toMillis(nanoTime() - startedAt)
            remaining = (remaining - elapsedMs).coerceAtLeast(0L)
        }
    }
}
