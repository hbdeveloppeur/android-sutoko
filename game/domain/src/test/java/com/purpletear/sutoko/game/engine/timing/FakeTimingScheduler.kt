package com.purpletear.sutoko.game.engine.timing

/**
 * Test double for TimingScheduler.
 * Allows manual control of time for testing timing-dependent logic.
 */
class FakeTimingScheduler : TimingScheduler {
    
    private val delayCalls = mutableListOf<Long>()
    
    override suspend fun delay(millis: Long) {
        if (millis > 0) {
            delayCalls.add(millis)
        }
    }
    
    /**
     * Returns the list of all delay calls made.
     */
    fun getDelayCalls(): List<Long> = delayCalls.toList()
    
    /**
     * Returns the total delayed time.
     */
    fun getTotalDelayedTime(): Long = delayCalls.sum()
    
    /**
     * Resets the scheduler state.
     */
    fun reset() {
        delayCalls.clear()
    }
}
