package fr.purpletear.sutoko.ads

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.android.ump.UserMessagingPlatform
import com.purpletear.ads.InterstitialGateway
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import fr.purpletear.sutoko.BuildConfig
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@EntryPoint
@InstallIn(SingletonComponent::class)
interface InterstitialPlaybackEntryPoint { fun gateway(): InterstitialGateway }

@RunWith(AndroidJUnit4::class)
class InterstitialPlaybackTest {
    @Test fun googleTestInterstitialLoadsShowsAndDismissesExactlyOnce() {
        assertTrue(BuildConfig.DEBUG)
        assertEquals("ca-app-pub-3940256099942544/1033173712", BuildConfig.ADMOB_INTERSTITIAL_CHAPTER_UNIT_ID)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        // Do not reset or choose the account owner's privacy preferences for a smoke test.
        assumeTrue("Device has no ad-request consent; complete UMP on the debug app first",
            UserMessagingPlatform.getConsentInformation(context).canRequestAds())
        val gateway = EntryPointAccessors.fromApplication(context, InterstitialPlaybackEntryPoint::class.java).gateway()
        val finished = CountDownLatch(1)
        val shown = CountDownLatch(1)
        val completions = AtomicInteger()
        val wasShown = AtomicBoolean(false)
        ActivityScenario.launch(AdPlaybackTestActivity::class.java).use { scenario ->
            scenario.onActivity { gateway.preload() }
            val ready = AtomicBoolean(false)
            val deadline = android.os.SystemClock.elapsedRealtime() + 60_000
            while (!ready.get() && android.os.SystemClock.elapsedRealtime() < deadline) {
                instrumentation.runOnMainSync { ready.set(gateway.ready) }
                android.os.SystemClock.sleep(200)
            }
            assertTrue("Google test inventory did not load within 60 seconds", ready.get())
            scenario.onActivity { host ->
                gateway.show(host, "instrumentation", "test", { shown.countDown() }) { displayed ->
                    wasShown.set(displayed)
                    completions.incrementAndGet()
                    finished.countDown()
                }
            }
            assertTrue("Fullscreen show callback missing", shown.await(15, TimeUnit.SECONDS))
            android.os.SystemClock.sleep(12_000)
            // Back dismisses the test ad, without tapping any advertiser link or install button.
            val device = UiDevice.getInstance(instrumentation)
            repeat(3) {
                if (finished.count > 0) {
                    device.pressBack()
                    finished.await(3, TimeUnit.SECONDS)
                }
            }
            assertTrue("Test ad did not dismiss", finished.await(5, TimeUnit.SECONDS))
            assertTrue(wasShown.get())
            assertEquals(1, completions.get())
        }
    }
}
