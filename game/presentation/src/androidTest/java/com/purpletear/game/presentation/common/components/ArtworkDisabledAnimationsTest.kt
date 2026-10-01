package com.purpletear.game.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class ArtworkDisabledAnimationsTest {
    @get:Rule val compose = createComposeRule(object : MotionDurationScale {
        override val scaleFactor = 0f
    })

    @Test
    fun reducedMotionSkipsTheFadeAndArtworkCoordinationWait() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            Box(Modifier.size(100.dp).background(Color.Black).testTag("surface")) {
                ArtworkReveal(isReady = false, isCached = false) {
                    Box(Modifier.size(100.dp).background(Color.White))
                }
            }
        }
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeByFrame()
        val pixels = compose.onNodeWithTag("surface").captureToImage().toPixelMap()
        assertTrue("Reduced motion left artwork hidden", pixels[pixels.width / 2, pixels.height / 2].red > 0.99f)
    }
}
