package fr.purpletear.sutoko.screens.main.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.purpletear.sutoko.screens.main.presentation.screens.home.components.HomeEntrance
import fr.purpletear.sutoko.screens.main.presentation.screens.home.components.rememberHomeContentEntrance
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeEntranceTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun entranceFadesWithoutChangingLayoutHeight() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            val animate = rememberHomeContentEntrance(hasStories = true)
            Box(Modifier.size(100.dp).background(Color.Black).testTag("surface")) {
                HomeEntrance(animate = animate, modifier = Modifier.testTag("entrance")) {
                    Box(Modifier.size(100.dp).background(Color.White))
                }
            }
        }
        val startHeight = compose.onNodeWithTag("entrance").fetchSemanticsNode().boundsInRoot.height
        compose.mainClock.advanceTimeBy(120)
        val during = brightness("surface")
        assertTrue("Expected a gradual fade, got $during", during > 0.05f && during < 0.99f)
        compose.mainClock.advanceTimeBy(400)
        assertTrue(brightness("surface") > 0.99f)
        val endHeight = compose.onNodeWithTag("entrance").fetchSemanticsNode().boundsInRoot.height
        assertTrue("Entrance changed layout height", kotlin.math.abs(startHeight - endHeight) < 1f)
    }

    @Test
    fun returningToContentDoesNotReplayItsEntrance() {
        val visible = mutableStateOf(true)
        compose.mainClock.autoAdvance = false
        compose.setContent {
            val animate = rememberHomeContentEntrance(hasStories = true)
            Box(Modifier.size(100.dp).background(Color.Black).testTag("surface")) {
                if (visible.value) HomeEntrance(animate = animate) {
                    Box(Modifier.size(100.dp).background(Color.White))
                }
            }
        }
        compose.mainClock.advanceTimeBy(450)
        compose.runOnIdle { visible.value = false }
        compose.mainClock.advanceTimeByFrame()
        compose.runOnIdle { visible.value = true }
        compose.mainClock.advanceTimeByFrame()
        assertTrue("Return replayed the fade", brightness("surface") > 0.99f)
    }

    @Test
    fun offscreenItemsAreImmediatelyVisibleWhenScrolledIntoView() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            val animate = rememberHomeContentEntrance(hasStories = true)
            LazyColumn(Modifier.height(100.dp).testTag("list")) {
                items(20, key = { it }) { index ->
                    HomeEntrance(animate = animate) {
                        Box(Modifier.size(100.dp).background(Color.White).testTag("item_$index"))
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(450)
        compose.onNodeWithTag("list").performScrollToIndex(12)
        compose.mainClock.advanceTimeByFrame()
        assertTrue("Scrolled content replayed the fade", brightness("item_12") > 0.99f)
    }

    @Test
    fun restoredHomeDoesNotReplayItsEntrance() {
        compose.mainClock.autoAdvance = false
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            val animate = rememberHomeContentEntrance(hasStories = true)
            Box(Modifier.size(100.dp).background(Color.Black).testTag("surface")) {
                HomeEntrance(animate = animate) {
                    Box(Modifier.size(100.dp).background(Color.White))
                }
            }
        }
        compose.mainClock.advanceTimeBy(450)
        restoration.emulateSavedInstanceStateRestore()
        compose.mainClock.advanceTimeByFrame()
        assertTrue("Restoration replayed the fade", brightness("surface") > 0.99f)
    }

    @Test
    fun loadingDoesNotConsumeTheContentEntrance() {
        val loaded = mutableStateOf(false)
        compose.mainClock.autoAdvance = false
        compose.setContent {
            val animate = rememberHomeContentEntrance(hasStories = loaded.value)
            Box(Modifier.size(100.dp).background(Color.Black).testTag("surface")) {
                if (loaded.value) HomeEntrance(animate = animate) {
                    Box(Modifier.size(100.dp).background(Color.White))
                }
            }
        }
        compose.mainClock.advanceTimeBy(500)
        compose.runOnIdle { loaded.value = true }
        compose.mainClock.advanceTimeBy(120)
        val during = brightness("surface")
        assertTrue("First loaded content did not fade: $during", during > 0.05f && during < 0.99f)
        compose.mainClock.advanceTimeBy(400)
        assertTrue(brightness("surface") > 0.99f)
    }

    private fun brightness(tag: String): Float {
        val pixels = compose.onNodeWithTag(tag).captureToImage().toPixelMap()
        return pixels[pixels.width / 2, pixels.height / 2].red
    }
}
