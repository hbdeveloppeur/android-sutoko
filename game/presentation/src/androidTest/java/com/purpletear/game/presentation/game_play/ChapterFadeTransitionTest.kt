package com.purpletear.game.presentation.game_play

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChapterFadeTransitionTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var scope: CoroutineScope
    private lateinit var transition: GameFadeTransition

    @Test
    fun chapterChangesOnlyAtBlackAndWaitsForContentBeforeFadingIn() {
        setUpTransition()
        compose.runOnIdle {
            assertEquals(1f, transition.alpha, 0f)
            assertTrue(transition.blocksInput)
            scope.launch { transition.reveal() }
        }
        advance(250)
        compose.runOnIdle { assertTrue(transition.alpha in 0.01f..0.99f) }
        advance(600)
        compose.runOnIdle {
            assertEquals(0f, transition.alpha, 0f)
            assertFalse(transition.blocksInput)
        }

        val ready = CompletableDeferred<Boolean>()
        var chapterChanged = false
        compose.runOnIdle {
            scope.launch {
                transition.fadeThroughBlack {
                    assertEquals(1f, transition.alpha, 0f)
                    chapterChanged = true
                    ready.await()
                }
            }
        }
        advance(250)
        compose.runOnIdle {
            assertFalse(chapterChanged)
            assertTrue(transition.alpha in 0.01f..0.99f)
            assertTrue(transition.blocksInput)
        }
        advance(600)
        compose.runOnIdle {
            assertTrue(chapterChanged)
            assertEquals(1f, transition.alpha, 0f)
            scope.launch { transition.reveal() }
        }
        advance(2_000)
        compose.runOnIdle {
            assertEquals(1f, transition.alpha, 0f)
            ready.complete(true)
        }
        advance(250)
        compose.runOnIdle {
            assertTrue(transition.alpha in 0.01f..0.99f)
            assertTrue(transition.blocksInput)
        }
        advance(600)
        compose.runOnIdle {
            assertEquals(0f, transition.alpha, 0f)
            assertFalse(transition.blocksInput)
        }
    }

    @Test
    fun failedContentRemainsCoveredAndDuplicateChapterRequestsAreIgnored() {
        setUpTransition()
        compose.runOnIdle { scope.launch { transition.reveal() } }
        advance(600)
        val ready = CompletableDeferred<Boolean>()
        var changes = 0
        compose.runOnIdle {
            scope.launch { transition.fadeThroughBlack { changes++; ready.await() } }
            scope.launch { transition.fadeThroughBlack { changes++; true } }
        }
        advance(600)
        compose.runOnIdle {
            assertEquals(1, changes)
            ready.complete(false)
        }
        advance(1_000)
        compose.runOnIdle {
            assertEquals(1f, transition.alpha, 0f)
            assertTrue(transition.blocksInput)
        }
    }

    @Test
    fun automaticNavigationDuringInitialRevealIsNotLost() {
        setUpTransition()
        compose.runOnIdle { scope.launch { transition.reveal() } }
        advance(200)
        var changes = 0
        compose.runOnIdle {
            scope.launch {
                transition.fadeThroughBlack {
                    assertEquals(1f, transition.alpha, 0f)
                    changes++
                    true
                }
            }
        }
        advance(200)
        compose.runOnIdle { assertEquals(0, changes) }
        advance(2_000)
        compose.runOnIdle {
            assertEquals(1, changes)
            assertEquals(0f, transition.alpha, 0f)
            assertFalse(transition.blocksInput)
        }
    }

    @Test
    fun interruptedRevealCanResumeWithoutLeavingInputLocked() {
        setUpTransition()
        val reveal = compose.runOnIdle { scope.launch { transition.reveal() } }
        advance(200)
        compose.runOnIdle { reveal.cancel() }
        advance(50)
        compose.runOnIdle { scope.launch { transition.reveal() } }
        advance(600)
        compose.runOnIdle {
            assertEquals(0f, transition.alpha, 0f)
            assertFalse(transition.blocksInput)
        }
    }

    @Test
    fun navHostDrawsFadeOutBlackThenFadeInWithoutMixingChapters() {
        compose.mainClock.autoAdvance = false
        transition = GameFadeTransition()
        lateinit var navController: NavHostController
        compose.setContent {
            scope = rememberCoroutineScope()
            navController = rememberNavController()
            val overlayAlpha = remember { { transition.alpha } }
            Box(Modifier.fillMaxSize().testTag("story")) {
                SmsGameNavHost(navController, "chapter/1", overlayAlpha) {
                    composable("chapter/{code}") { entry ->
                        val color = if (entry.arguments?.getString("code") == "1") Color.Red else Color.Green
                        Box(Modifier.fillMaxSize().background(color))
                    }
                }
            }
        }
        assertPixel(Color.Black)
        compose.runOnIdle { scope.launch { transition.reveal() } }
        advance(600)
        assertPixel(Color.Red)
        val ready = CompletableDeferred<Boolean>()
        compose.runOnIdle {
            scope.launch {
                transition.fadeThroughBlack {
                    navController.navigate("chapter/2") {
                        popUpTo("chapter/{code}") { inclusive = true }
                    }
                    ready.await()
                }
            }
        }
        advance(250)
        val outgoing = pixel()
        assertTrue(outgoing.red in 0.01f..0.99f)
        assertEquals(0f, outgoing.green, 0.01f)
        advance(600)
        assertPixel(Color.Black)
        compose.runOnIdle { ready.complete(true) }
        advance(250)
        val incoming = pixel()
        assertTrue(incoming.green in 0.01f..0.99f)
        assertEquals(0f, incoming.red, 0.01f)
        advance(600)
        assertPixel(Color.Green)
    }

    private fun pixel(): Color {
        val pixels = compose.onNodeWithTag("story").captureToImage().toPixelMap()
        return pixels[pixels.width / 2, pixels.height / 2]
    }

    private fun assertPixel(expected: Color) {
        val actual = pixel()
        assertEquals(expected.red, actual.red, 0.01f)
        assertEquals(expected.green, actual.green, 0.01f)
        assertEquals(expected.blue, actual.blue, 0.01f)
    }

    private fun setUpTransition() {
        compose.mainClock.autoAdvance = false
        transition = GameFadeTransition()
        compose.setContent { scope = rememberCoroutineScope() }
    }

    private fun advance(millis: Long) {
        compose.mainClock.advanceTimeBy(millis)
        compose.waitForIdle()
    }
}
