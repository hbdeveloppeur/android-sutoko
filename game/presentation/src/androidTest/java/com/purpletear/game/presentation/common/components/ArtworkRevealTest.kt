package com.purpletear.game.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArtworkRevealTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun readyArtworkFadesAsOneLayerWithoutMovingItsLayout() {
        val ready = mutableStateOf(false)
        compose.mainClock.autoAdvance = false
        compose.setContent {
            Box(Modifier.size(100.dp).background(Color.Black).testTag("surface")) {
                ArtworkReveal(isReady = ready.value, isCached = false, modifier = Modifier.testTag("artwork")) {
                    Box(Modifier.size(100.dp).background(Color.White))
                }
            }
        }
        val heightBefore = compose.onNodeWithTag("artwork").fetchSemanticsNode().boundsInRoot.height
        compose.runOnIdle { ready.value = true }
        compose.mainClock.advanceTimeBy(64)
        val during = brightness()
        assertTrue("Artwork should fade, not pop: $during", during > 0.05f && during < 0.99f)
        compose.mainClock.advanceTimeBy(300)
        assertTrue(brightness() > 0.99f)
        assertEquals(heightBefore, compose.onNodeWithTag("artwork").fetchSemanticsNode().boundsInRoot.height, 1f)
    }

    @Test
    fun cachedArtworkIsImmediatelyVisible() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            Box(Modifier.size(100.dp).background(Color.Black).testTag("surface")) {
                ArtworkReveal(isReady = true, isCached = true) {
                    Box(Modifier.size(100.dp).background(Color.White))
                }
            }
        }
        assertTrue("Cached artwork replayed its fade", brightness() > 0.99f)
    }

    @Test
    fun slowAssetCannotKeepArtworkHiddenIndefinitely() {
        compose.setContent {
            Box(Modifier.size(100.dp).background(Color.Black).testTag("surface")) {
                ArtworkReveal(isReady = false, isCached = false) {
                    Box(Modifier.size(100.dp).background(Color.White))
                }
            }
        }
        compose.waitUntil(timeoutMillis = 3_000) { brightness() > 0.99f }
    }

    @Test
    fun refreshDoesNotHideAlreadyRevealedArtwork() {
        val ready = mutableStateOf(true)
        compose.mainClock.autoAdvance = false
        compose.setContent {
            Box(Modifier.size(100.dp).background(Color.Black).testTag("surface")) {
                ArtworkReveal(isReady = ready.value, isCached = true) {
                    Box(Modifier.size(100.dp).background(Color.White))
                }
            }
        }
        compose.runOnIdle { ready.value = false }
        compose.mainClock.advanceTimeByFrame()
        assertTrue("Refresh hid revealed artwork", brightness() > 0.99f)
    }

    @Test
    fun restoredArtworkDoesNotWaitForImagesOrReplayTheFade() {
        val ready = mutableStateOf(true)
        val cached = mutableStateOf(true)
        compose.mainClock.autoAdvance = false
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            Box(Modifier.size(100.dp).background(Color.Black).testTag("surface")) {
                ArtworkReveal(isReady = ready.value, isCached = cached.value) {
                    Box(Modifier.size(100.dp).background(Color.White))
                }
            }
        }
        compose.mainClock.advanceTimeByFrame()
        compose.runOnIdle {
            ready.value = false
            cached.value = false
        }
        restoration.emulateSavedInstanceStateRestore()
        compose.mainClock.advanceTimeByFrame()
        assertTrue("Restored artwork replayed its loading gate", brightness() > 0.99f)
    }

    private fun brightness(): Float {
        val pixels = compose.onNodeWithTag("surface").captureToImage().toPixelMap()
        return pixels[pixels.width / 2, pixels.height / 2].red
    }
}
