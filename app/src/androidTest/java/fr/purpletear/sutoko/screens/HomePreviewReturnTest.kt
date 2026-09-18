package fr.purpletear.sutoko.screens

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.PixelCopy
import android.view.Window
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import java.util.regex.Pattern
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Device smoke test using the official catalog, independent of the catalog's display language. */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 26)
class HomePreviewReturnTest {
    @Test
    fun returningFromPreviewDoesNotReplayHomeBlackOverlay() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            lateinit var window: Window
            scenario.onActivity { window = it.window }
            val home = By.res("home_screen").pkg("fr.purpletear.sutoko")
            assertTrue("Home did not load", device.wait(Until.hasObject(home), 30_000))
            val story = requireNotNull(device.wait(
                Until.findObject(By.pkg("fr.purpletear.sutoko")
                    .text(Pattern.compile("Friendzon.*", Pattern.CASE_INSENSITIVE))),
                30_000,
            )) { "Official story catalog did not load" }
            device.waitForIdle()
            val homeBrightness = screenshotBrightness(window)
            assertTrue("Reference Home frame is blank", homeBrightness > 0.05f)
            story.click()
            assertTrue(device.wait(Until.gone(home), 10_000))
            device.waitForIdle()
            // Accessibility can hide Home before its exit finishes. Let it leave composition fully.
            SystemClock.sleep(1_000)

            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            val returnStartedAt = SystemClock.uptimeMillis()
            // Sample after the 300 ms Preview exit, before an erroneous Home reveal finishes.
            SystemClock.sleep(350)
            val returnedBrightness = screenshotBrightness(window)
            Log.i("HomePreviewReturnTest", "Home brightness $homeBrightness -> $returnedBrightness, captured after ${SystemClock.uptimeMillis() - returnStartedAt} ms")
            assertTrue(
                "Home is obscured after Preview exits: $returnedBrightness vs $homeBrightness",
                returnedBrightness >= homeBrightness * 0.8f,
            )
            assertTrue(device.wait(Until.hasObject(home), 2_000))
        }
    }

    private fun screenshotBrightness(window: Window): Float {
        val bitmap = Bitmap.createBitmap(window.decorView.width, window.decorView.height, Bitmap.Config.ARGB_8888)
        val captured = CountDownLatch(1)
        var result = PixelCopy.ERROR_UNKNOWN
        PixelCopy.request(window, bitmap, {
            result = it
            captured.countDown()
        }, Handler(Looper.getMainLooper()))
        return try {
            assertTrue("Timed out capturing the current frame", captured.await(3, TimeUnit.SECONDS))
            assertEquals(PixelCopy.SUCCESS, result)
            bitmap.meanBrightness()
        } finally {
            bitmap.recycle()
        }
    }

    private fun Bitmap.meanBrightness(): Float {
        var sum = 0f
        var count = 0
        for (y in height * 15 / 100 until height * 85 / 100 step 8) {
            for (x in width / 10 until width * 9 / 10 step 8) {
                val pixel = getPixel(x, y)
                sum += (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / (3f * 255f)
                count++
            }
        }
        return sum / count
    }
}
