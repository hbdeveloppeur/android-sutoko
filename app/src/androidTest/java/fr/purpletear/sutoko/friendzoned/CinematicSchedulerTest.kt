package fr.purpletear.sutoko.friendzoned

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import purpletear.fr.purpleteartools.CinematicScheduler

@RunWith(AndroidJUnit4::class)
class CinematicSchedulerTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test fun resumesPendingCueOnceAndCancelsOnExit() {
        var calls = 0
        lateinit var scheduler: CinematicScheduler
        instrumentation.runOnMainSync {
            scheduler = CinematicScheduler()
            scheduler.schedule(300) { calls++ }
            scheduler.resume()
        }
        SystemClock.sleep(100)
        instrumentation.runOnMainSync { scheduler.pause() }
        SystemClock.sleep(400)
        instrumentation.runOnMainSync { assertEquals(0, calls); scheduler.resume(); scheduler.resume() }
        SystemClock.sleep(500)
        instrumentation.runOnMainSync {
            assertEquals(1, calls)
            scheduler.schedule(100) { calls++ }
            scheduler.clear()
        }
        SystemClock.sleep(300)
        instrumentation.runOnMainSync { assertEquals(1, calls) }
    }
}
