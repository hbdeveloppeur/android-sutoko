package fr.purpletear.sutoko.ads

import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.purpletear.ads.ChapterBreakGate
import com.purpletear.ads.ReaderAds
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class ChapterBreakGateLifecycleTest {
    @Test fun ownedReaderContinuesOncePerChildResultAcrossResume() {
        val launches = AtomicInteger()
        ActivityScenario.launch(AdPlaybackTestActivity::class.java).use { scenario ->
            var gate: ChapterBreakGate? = null
            scenario.onActivity { activity ->
                gate = ChapterBreakGate(activity, null, 162, true, Runnable { launches.incrementAndGet() })
            }
            assertEquals(1, launches.get())
            scenario.onActivity {
                gate!!.childReturned(Intent().putExtra(ReaderAds.CHAPTER_TRANSITION, "transition"), 5)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            assertEquals(2, launches.get())
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
            assertEquals(2, launches.get())
        }
    }

    @Test fun restoredLoaderDoesNotLaunchAnotherChildUntilResultArrives() {
        val launches = AtomicInteger()
        val saved = Bundle()
        ActivityScenario.launch(AdPlaybackTestActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val gate = ChapterBreakGate(activity, null, 163, true, Runnable { launches.incrementAndGet() })
                gate.save(saved)
            }
            assertEquals(1, launches.get())
            scenario.recreate()
            scenario.onActivity { activity ->
                ChapterBreakGate(activity, saved, 163, true, Runnable { launches.incrementAndGet() })
            }
            assertEquals(1, launches.get())
        }
    }
}
