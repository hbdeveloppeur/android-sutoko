package com.purpletear.sutoko.game.engine.timing

/**
 * Test double for TimingScheduler.
 * Allows manual control of time for testing timing-dependent logic.
 */
class FakeTimingScheduler : TimingScheduler {
    
    private var currentTime: Long = 0
    private val delayCalls = mutableListOf<Long>()
    
    override suspend fun delay(millis: Long) {
        if (millis > 0) {
            delayCalls.add(millis)
            currentTime += millis
        }
    }
    
    override fun now(): Long {
        return currentTime
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
     * Advances time by the specified amount without suspending.
     */
    fun advanceTimeBy(millis: Long) {
        currentTime += millis
    }
    
    /**
     * Resets the scheduler state.
     */
    fun reset() {
        currentTime = 0
        delayCalls.clear()
    }
}
