package fr.purpletear.sutoko.screens.splashscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class SplashSnowCadenceTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun laterFlakesAppearAfterTheFirstFrame() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            Box(Modifier.fillMaxSize().background(Color.Black).testTag("snowfall")) {
                SplashSnowOverlay(fadingOut = false, onFadeFinished = {})
            }
        }

        fun hasSnow(left: Float, right: Float, top: Float, bottom: Float): Boolean {
            val pixels = compose.onNodeWithTag("snowfall").captureToImage().toPixelMap()
            val columns = (pixels.width * left).toInt() until (pixels.width * right).toInt()
            val rows = (pixels.height * top).toInt() until (pixels.height * bottom).toInt()
            return rows.step(2).any { y -> columns.step(2).any { x -> pixels[x, y].green > 0.38f } }
        }

        assertTrue(hasSnow(left = 0.37f, right = 0.65f, top = 0.4f, bottom = 0.6f))
        assertFalse(hasSnow(left = 0.63f, right = 0.9f, top = 0.08f, bottom = 0.29f))
        compose.mainClock.advanceTimeBy(530)
        assertTrue(hasSnow(left = 0.63f, right = 0.9f, top = 0.08f, bottom = 0.29f))
    }
}
