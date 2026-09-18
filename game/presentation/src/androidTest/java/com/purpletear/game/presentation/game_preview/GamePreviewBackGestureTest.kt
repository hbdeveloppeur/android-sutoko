package com.purpletear.game.presentation.game_preview

import android.view.TextureView
import android.view.ViewGroup
import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.descendants
import androidx.lifecycle.Lifecycle
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GamePreviewBackGestureTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()
    private lateinit var navController: NavHostController

    @Before
    fun openVideoPreview() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val video = File(instrumentation.targetContext.cacheDir, "preview-transition.mp4")
        instrumentation.context.assets.open("preview-transition.mp4").use { source ->
            video.outputStream().use(source::copyTo)
        }
        compose.setContent {
            navController = rememberNavController()
            NavHost(navController, startDestination = "home") {
                composable(
                    "home",
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                ) {
                    Box(Modifier.fillMaxSize().background(Color.Blue))
                }
                composable(
                    "preview",
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            tween(300),
                            initialOffset = { it / 4 },
                        ) + fadeIn(tween(300))
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            tween(300),
                            targetOffset = { it / 4 },
                        ) + fadeOut(tween(300))
                    },
                ) {
                    GameBackgroundPreviewMedia(
                        imageUrl = null,
                        videoUrl = video.toURI().toString(),
                        fallbackPainter = ColorPainter(Color.Red),
                        overlayAlpha = 0f,
                    )
                }
            }
        }
        compose.runOnIdle { navController.navigate("preview") }
        compose.waitUntil(10_000) {
            var ready = false
            compose.runOnUiThread {
                ready = playerView()?.let {
                    it.player?.isPlaying == true && hasVideoFrame(it)
                } == true
            }
            ready
        }
        compose.waitForIdle()
    }

    @Test
    fun cancelledBackKeepsVideoSurfaceAndResumesSamePlayer() {
        lateinit var originalView: PlayerView
        compose.runOnIdle { originalView = requireNotNull(playerView()) }
        val originalPlayer = originalView.player
        val originalSurface = originalView.videoSurfaceView

        startBack()
        compose.runOnUiThread {
            compose.activity.onBackPressedDispatcher.dispatchOnBackProgressed(backEvent(0.5f))
        }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(Lifecycle.State.STARTED, navController.currentBackStackEntry?.lifecycle?.currentState)
            assertSame(originalView, playerView())
            assertSame(originalPlayer, originalView.player)
            assertSame(originalSurface, originalView.videoSurfaceView)
            assertFalse(requireNotNull(originalPlayer).isPlaying)
            assertTrue("Paused video lost its rendered frame during swipe", hasVideoFrame(originalView))
            compose.activity.onBackPressedDispatcher.dispatchOnBackCancelled()
        }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals("preview", navController.currentDestination?.route)
            assertEquals(Lifecycle.State.RESUMED, navController.currentBackStackEntry?.lifecycle?.currentState)
            assertSame(originalPlayer, requireNotNull(playerView()).player)
            assertTrue(requireNotNull(originalPlayer).playWhenReady)
            assertTrue("Cancelled swipe cleared the video", hasVideoFrame(originalView))
        }
        compose.waitUntil(2_000) {
            var playing = false
            compose.runOnUiThread { playing = requireNotNull(originalPlayer).isPlaying }
            playing
        }
    }

    @Test
    @Ignore("Navigation 2.9.6 leaves the entry STARTED after start/cancel without progress. Re-enable after upgrading Navigation.")
    fun cancelledBackWithoutProgressResumesPreview() {
        startBack()
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.dispatchOnBackCancelled() }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(Lifecycle.State.RESUMED, navController.currentBackStackEntry?.lifecycle?.currentState)
            assertTrue(requireNotNull(playerView()?.player).playWhenReady)
        }
    }

    @Test
    fun committedBackRetainsVideoUntilExitCompletes() {
        startBack()
        compose.runOnUiThread {
            compose.activity.onBackPressedDispatcher.dispatchOnBackProgressed(backEvent(0.25f))
        }
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeBy(32)
        compose.runOnUiThread {
            assertTrue("Video disappeared before exit completed", hasVideoFrame(requireNotNull(playerView())))
        }
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals("home", navController.currentDestination?.route)
            assertEquals(null, playerView())
        }
    }

    private fun startBack() {
        compose.runOnUiThread {
            compose.activity.onBackPressedDispatcher.dispatchOnBackStarted(backEvent(0f))
        }
        compose.waitForIdle()
    }

    private fun backEvent(progress: Float) = BackEventCompat(0f, 100f, progress, BackEventCompat.EDGE_LEFT)

    private fun playerView(): PlayerView? =
        (compose.activity.window.decorView as ViewGroup).descendants.filterIsInstance<PlayerView>().singleOrNull()

    private fun hasVideoFrame(view: PlayerView): Boolean {
        val bitmap = (view.videoSurfaceView as? TextureView)?.bitmap ?: return false
        try {
            for (y in 0 until bitmap.height step 16) {
                for (x in 0 until bitmap.width step 16) {
                    if (bitmap.getPixel(x, y) and 0x00ffffff != 0) return true
                }
            }
            return false
        } finally {
            bitmap.recycle()
        }
    }
}
