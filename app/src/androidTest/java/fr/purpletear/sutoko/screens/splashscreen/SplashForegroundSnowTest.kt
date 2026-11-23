package fr.purpletear.sutoko.screens.splashscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class SplashForegroundSnowTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun snowIsVisibleOverTheSplashArtworkOnTheFirstFrame() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.fillMaxWidth(0.6f).height(104.dp)
                        .background(Color(0xFF501D4F)).testTag("splash artwork"),
                )
                SplashSnowOverlay(fadingOut = false, onFadeFinished = {})
            }
        }

        val artwork = compose.onNodeWithTag("splash artwork").captureToImage().toPixelMap()
        val snowVisible = (0 until artwork.height step 2).any { y ->
            (0 until artwork.width step 2).any { x -> artwork[x, y].green > 0.45f }
        }
        assertTrue("Snow should be visible in front of the splash artwork", snowVisible)
    }
}
