package com.purpletear.game.data.infrastructure

import com.purpletear.sutoko.game.engine.timing.TimingScheduler
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of TimingScheduler using Kotlin coroutines.
 * 
 * Located in Infrastructure layer as it depends on:
 * - kotlinx.coroutines (external framework)
 * - System.currentTimeMillis() (system resource)
 * 
 * This is NOT a Presentation concern - it's a framework/driver implementation detail.
 */
@Singleton
class SystemTimingScheduler @Inject constructor() : TimingScheduler {
    
    override suspend fun delay(millis: Long) {
        if (millis > 0) {
            delay(millis)
        }
    }
    
    override fun now(): Long {
        return System.currentTimeMillis()
    }
}
