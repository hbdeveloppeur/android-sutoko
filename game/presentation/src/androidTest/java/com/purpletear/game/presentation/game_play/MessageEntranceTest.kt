package com.purpletear.game.presentation.game_play

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.purpletear.game.presentation.game_play.components.message.FadeInMessageContainer
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageEntranceTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun fadeRetainsLayoutAndCompletesAfterEligibilityIsConsumed() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            Box(Modifier.size(60.dp).background(Color.Black).testTag("surface")) {
                FadeInMessageContainer(
                    animate = true,
                    durationMillis = 180,
                ) { Box(Modifier.size(60.dp).background(Color.White).testTag("message")) }
            }
        }
        compose.onNodeWithTag("message").assertHeightIsEqualTo(60.dp)
        assertTrue(brightness() < 0.1f)
        compose.mainClock.advanceTimeBy(96)
        assertTrue(brightness() in 0.1f..0.95f)
        compose.mainClock.advanceTimeBy(160)
        assertTrue(brightness() > 0.99f)
    }

    @Test
    fun lazyRowComposedLaterFadesOnlyOnceAcrossScrolling() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            LazyColumn(Modifier.size(60.dp).background(Color.Black).testTag("surface")) {
                items(40, key = { it }) { _ ->
                    FadeInMessageContainer(animate = true, durationMillis = 180) {
                        Box(Modifier.size(60.dp).background(Color.White))
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(256)
        compose.onNodeWithTag("surface").performScrollToIndex(30)
        compose.mainClock.advanceTimeByFrame()
        assertTrue(brightness() < 0.1f)
        compose.mainClock.advanceTimeBy(256)
        assertTrue(brightness() > 0.99f)
        compose.onNodeWithTag("surface").performScrollToIndex(0)
        compose.mainClock.advanceTimeByFrame()
        assertTrue(brightness() > 0.99f)
        compose.onNodeWithTag("surface").performScrollToIndex(30)
        compose.mainClock.advanceTimeByFrame()
        assertTrue(brightness() > 0.99f)
    }

    private fun brightness(): Float {
        val pixels = compose.onNodeWithTag("surface").captureToImage().toPixelMap()
        return pixels[pixels.width / 2, pixels.height / 2].red
    }
}
