package com.purpletear.game.data.infrastructure

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemTimingSchedulerTest {

    /**
     * Deterministic, manually advanced nano clock: the scheduler measures elapsed time
     * through it, so tests decide exactly how much "real" time passed before a hold.
     */
    private class FakeNanoClock {
        var nanos = 0L
        fun advanceByMs(ms: Long) {
            nanos += TimeUnit.MILLISECONDS.toNanos(ms)
        }
    }

    @Test
    fun `delay completes normally when never held`() = runTest {
        val scheduler = SystemTimingScheduler()

        val job = launch { scheduler.delay(500) }
        runCurrent()
        assertFalse(job.isCompleted)

        advanceTimeBy(500)
        runCurrent()
        assertTrue(job.isCompleted)
    }

    @Test
    fun `delay parks while held and resumes after release`() = runTest {
        val scheduler = SystemTimingScheduler()
        scheduler.setHoldPaused(true)

        val job = launch { scheduler.delay(500) }
        advanceTimeBy(10_000)
        runCurrent()
        assertFalse(job.isCompleted)

        scheduler.setHoldPaused(false)
        runCurrent()
        assertFalse(job.isCompleted)

        advanceTimeBy(500)
        runCurrent()
        assertTrue(job.isCompleted)
    }

    @Test
    fun `hold mid delay freezes remaining time`() = runTest {
        val clock = FakeNanoClock()
        val scheduler = SystemTimingScheduler(nanoTime = clock::nanos)

        val job = launch { scheduler.delay(1_000) }
        runCurrent()

        // 400ms of the delay elapse, then the player holds.
        clock.advanceByMs(400)
        scheduler.setHoldPaused(true)
        runCurrent()

        // Held: no amount of wall time completes the delay.
        advanceTimeBy(10_000)
        runCurrent()
        assertFalse(job.isCompleted)

        // Release: only the 600ms remaining must elapse.
        scheduler.setHoldPaused(false)
        runCurrent()
        advanceTimeBy(599)
        runCurrent()
        assertFalse(job.isCompleted)

        advanceTimeBy(1)
        runCurrent()
        assertTrue(job.isCompleted)
    }

    @Test
    fun `zero delay completes immediately even while held`() = runTest {
        val scheduler = SystemTimingScheduler()
        scheduler.setHoldPaused(true)

        val job = launch { scheduler.delay(0) }
        runCurrent()
        assertTrue(job.isCompleted)
    }
}
