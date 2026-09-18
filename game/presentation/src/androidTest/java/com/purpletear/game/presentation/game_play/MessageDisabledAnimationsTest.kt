package com.purpletear.game.presentation.game_play

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
import com.purpletear.game.presentation.game_play.components.message.FadeInMessageContainer
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class MessageDisabledAnimationsTest {
    @get:Rule val compose = createComposeRule(object : MotionDurationScale {
        override val scaleFactor = 0f
    })

    @Test
    fun disabledAnimationsRevealTheMessageWithoutWaitingForTheFade() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            Box(Modifier.size(60.dp).background(Color.Black).testTag("surface")) {
                FadeInMessageContainer(animate = true, durationMillis = 180) {
                    Box(Modifier.size(60.dp).background(Color.White))
                }
            }
        }
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeByFrame()
        val pixels = compose.onNodeWithTag("surface").captureToImage().toPixelMap()
        assertTrue(pixels[pixels.width / 2, pixels.height / 2].red > 0.99f)
    }
}
