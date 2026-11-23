package fr.purpletear.sutoko.screens.splashscreen

import android.media.AudioAttributes
import android.media.AudioManager
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.test.core.app.ActivityScenario
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SdkSuppress(minSdkVersion = 26)
class SplashSoundTest {
    @Test
    fun soundStartsDuringVideoAndFinishesAfterSplashIsRemoved() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val audioManager = instrumentation.targetContext.getSystemService(AudioManager::class.java)

        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            val finished = CountDownLatch(1)
            assertFalse(audioManager.isSplashSoundPlaying())
            scenario.onActivity { activity ->
                activity.setContent {
                    VideoSplashScreen(onAnimationFinished = finished::countDown)
                }
            }
            assertTrue(audioManager.awaitSplashSound(playing = true, timeoutMillis = 8_000))
            assertTrue("Sound should start before video completion", finished.count > 0)
            assertTrue(finished.await(5, TimeUnit.SECONDS))
            scenario.onActivity { it.setContent {} }
            instrumentation.waitForIdleSync()
            assertTrue("Sound should outlive the splash composition", audioManager.isSplashSoundPlaying())
            assertTrue("Sound should play only once", audioManager.awaitSplashSound(playing = false, timeoutMillis = 6_000))
        }
    }

    @Test
    fun leavingForAnotherDestinationStopsTheChime() {
        val audioManager = InstrumentationRegistry.getInstrumentation().targetContext
            .getSystemService(AudioManager::class.java)

        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            val finished = CountDownLatch(1)
            scenario.onActivity { activity ->
                activity.setContent {
                    VideoSplashScreen(
                        onAnimationFinished = finished::countDown,
                        shouldFinishSound = { false },
                    )
                }
            }
            assertTrue(audioManager.awaitSplashSound(playing = true, timeoutMillis = 8_000))
            assertTrue(finished.await(5, TimeUnit.SECONDS))
            assertTrue(audioManager.isSplashSoundPlaying())
            scenario.onActivity { it.setContent {} }
            assertTrue(audioManager.awaitSplashSound(playing = false, timeoutMillis = 1_500))
        }
    }

    private fun AudioManager.isSplashSoundPlaying() = activePlaybackConfigurations.any {
        it.audioAttributes.usage == AudioAttributes.USAGE_MEDIA &&
            it.audioAttributes.contentType == AudioAttributes.CONTENT_TYPE_SONIFICATION
    }

    private fun AudioManager.awaitSplashSound(playing: Boolean, timeoutMillis: Long): Boolean {
        val deadline = SystemClock.elapsedRealtime() + timeoutMillis
        while (isSplashSoundPlaying() != playing && SystemClock.elapsedRealtime() < deadline) {
            SystemClock.sleep(20)
        }
        return isSplashSoundPlaying() == playing
    }
}
