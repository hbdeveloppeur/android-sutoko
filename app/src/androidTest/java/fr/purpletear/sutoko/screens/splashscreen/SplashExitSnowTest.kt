package fr.purpletear.sutoko.screens.splashscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
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
class SplashExitSnowTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun flakesRemainVisibleDuringTheFadeAndThenDisappear() {
        val showSnow = mutableStateOf(true)
        val fadingOut = mutableStateOf(false)
        compose.mainClock.autoAdvance = false
        compose.setContent {
            Box(Modifier.fillMaxSize().background(Color(0xFF301D42)).testTag("splash transition")) {
                if (showSnow.value) {
                    SplashSnowOverlay(fadingOut = fadingOut.value) { showSnow.value = false }
                }
            }
        }

        fun hasFlakesInHalf(top: Boolean): Boolean {
            val image = compose.onNodeWithTag("splash transition").captureToImage().toPixelMap()
            val rows = if (top) 0 until image.height / 2 else image.height / 2 until image.height
            return rows.step(2).any { y ->
                (0 until image.width step 2).any { x -> image[x, y].green > 0.28f }
            }
        }

        assertTrue(hasFlakesInHalf(top = true))
        assertTrue(hasFlakesInHalf(top = false))
        compose.runOnIdle { fadingOut.value = true }
        compose.mainClock.advanceTimeBy(210)
        assertTrue(hasFlakesInHalf(top = true))
        assertTrue(hasFlakesInHalf(top = false))
        compose.mainClock.advanceTimeBy(SplashExitDurationMillis.toLong() + 32)
        assertFalse(hasFlakesInHalf(top = false))
        compose.runOnIdle { assertFalse(showSnow.value) }
    }
}
