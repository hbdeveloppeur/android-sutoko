package fr.purpletear.sutoko.screens.splashscreen

import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class VideoSplashScreenTest {
    // Use a real activity clock: Compose test clocks would fast-forward the old timer.
    @Test
    fun fullRevealIsVisibleAndFinishesFasterWithoutTruncation() {
        Playback().use { playback ->
            playback.await("first video frame") { playback.firstFrame.get() > 0 }
            playback.await("visible logo") { playback.position() >= 4_100 }
            assertVisibleLogo(playback)
            playback.awaitFinished()
            playback.scenario.onActivity {
                val player = playback.player
                assertFalse(playback.playerView.useController)
                assertEquals(0f, player.volume)
                assertEquals(Player.REPEAT_MODE_OFF, player.repeatMode)
                assertEquals(2f, player.playbackParameters.speed)
                assertEquals(Player.STATE_ENDED, player.playbackState)
                assertEquals(6_006, player.duration)
                // The media clock can advance past the end when the emulator is under load.
                assertTrue(
                    "Video ended at ${player.currentPosition}ms for a ${player.duration}ms clip",
                    player.currentPosition >= player.duration - 100,
                )
                assertEquals(Player.STATE_ENDED, playback.stateAtCompletion)
            }
            assertEquals(1, playback.callbacks.get())
            val elapsed = playback.completion.get() - playback.start.get()
            val playingElapsed = playback.completion.get() - playback.playing.get()
            assertTrue("Full reveal took ${elapsed}ms", elapsed < 5_000)
            assertTrue("Playback was truncated: ${playingElapsed}ms", playingElapsed in 2_900..3_500)
            playback.scenario.onActivity {
                Log.i("SplashTiming", "completionMs=$elapsed, playingMs=${playback.playing.get() - playback.start.get()}, firstFrameMs=${playback.firstFrame.get() - playback.start.get()}, ended=true, positionMs=${playback.player.currentPosition}, durationMs=${playback.player.duration}, speed=${playback.player.playbackParameters.speed}, callbacks=${playback.callbacks.get()}")
            }
        }
    }

    @Test
    fun pausedPlaybackAndRecompositionDoNotFinishEarlyOrRestartTheVideo() {
        Playback().use { playback ->
            playback.await("first video frame") { playback.firstFrame.get() > 0 }
            val originalPlayer = playback.player
            val updatedCallbacks = AtomicInteger(0)
            playback.scenario.onActivity {
                playback.player.pause()
                playback.onFinished.value = {
                    updatedCallbacks.incrementAndGet()
                    playback.finish()
                }
            }
            SystemClock.sleep(3_500)
            assertEquals("A wall-clock timer completed the paused video", 0, playback.callbacks.get())
            playback.scenario.onActivity {
                assertSame(originalPlayer, findPlayer(it.window.decorView)!!.player)
                playback.player.play()
            }
            playback.awaitFinished()
            assertEquals("Completion used a stale callback", 1, updatedCallbacks.get())
            assertEquals(1, playback.callbacks.get())
        }
    }

    @Test
    fun playbackErrorsUnblockTheSplashOnlyOnce() {
        Playback().use { playback ->
            val errors = AtomicInteger(0)
            playback.scenario.onActivity {
                playback.player.addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        errors.incrementAndGet()
                    }
                })
                playback.player.setMediaItem(MediaItem.fromUri("android.resource://${it.packageName}/0"))
                playback.player.prepare()
            }
            playback.awaitFinished()
            playback.await("first playback error") { errors.get() == 1 }
            playback.scenario.onActivity { playback.player.prepare() }
            playback.await("second playback error") { errors.get() == 2 }
            assertEquals(1, playback.callbacks.get())
        }
    }

    @Test
    fun leavingTheSplashReleasesThePlayerWithoutCompletingIt() {
        val playback = Playback()
        playback.await("first video frame") { playback.firstFrame.get() > 0 }
        playback.close()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            assertEquals(Player.STATE_IDLE, playback.player.playbackState)
        }
        SystemClock.sleep(3_500)
        assertEquals(0, playback.callbacks.get())
    }

    private fun assertVisibleLogo(playback: Playback) {
        val location = IntArray(2)
        var width = 0
        var height = 0
        playback.scenario.onActivity {
            playback.playerView.getLocationOnScreen(location)
            width = playback.playerView.width
            height = playback.playerView.height
        }
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val screenshot = instrumentation.uiAutomation.takeScreenshot()
        try {
            var brightPixels = 0
            for (y in location[1] until location[1] + height step 4) {
                for (x in location[0] until location[0] + width step 4) {
                    val pixel = screenshot.getPixel(x, y)
                    if (android.graphics.Color.red(pixel) > 120) brightPixels++
                }
            }
            assertTrue("Video rendered a black/hidden logo", brightPixels > 100)
            File(instrumentation.targetContext.getExternalFilesDir(null), "splash-after.png").outputStream().use {
                screenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        } finally {
            screenshot.recycle()
        }
    }

    private class Playback : AutoCloseable {
        val start = AtomicLong(0)
        val completion = AtomicLong(0)
        val firstFrame = AtomicLong(0)
        val playing = AtomicLong(0)
        val callbacks = AtomicInteger(0)
        private val finished = CountDownLatch(1)
        var stateAtCompletion = Player.STATE_IDLE
        val onFinished = mutableStateOf<() -> Unit>({ finish() })
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        lateinit var playerView: PlayerView
        val player: Player get() = playerView.player!!

        init {
            scenario.onActivity { activity ->
                activity.setContent {
                    remember { start.set(SystemClock.elapsedRealtime()); true }
                    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                        VideoSplashScreen(Modifier.fillMaxWidth(0.65f), onAnimationFinished = onFinished.value)
                    }
                }
            }
            var attached = false
            await("PlayerView") {
                scenario.onActivity { activity ->
                    findPlayer(activity.window.decorView)?.let { view ->
                        if (!attached) {
                            playerView = view
                            player.addListener(object : Player.Listener {
                                override fun onRenderedFirstFrame() {
                                    firstFrame.compareAndSet(0, SystemClock.elapsedRealtime())
                                }
                                override fun onIsPlayingChanged(isPlaying: Boolean) {
                                    if (isPlaying) playing.compareAndSet(0, SystemClock.elapsedRealtime())
                                }
                            })
                            attached = true
                        }
                    }
                }
                attached
            }
        }

        fun finish() {
            stateAtCompletion = player.playbackState
            completion.compareAndSet(0, SystemClock.elapsedRealtime())
            callbacks.incrementAndGet()
            finished.countDown()
        }

        fun position(): Long {
            var position = 0L
            scenario.onActivity { position = player.currentPosition }
            return position
        }

        fun awaitFinished() {
            assertTrue("Splash did not finish", finished.await(12, TimeUnit.SECONDS))
        }

        fun await(description: String, condition: () -> Boolean) {
            val deadline = SystemClock.elapsedRealtime() + 12_000
            while (!condition() && SystemClock.elapsedRealtime() < deadline) SystemClock.sleep(10)
            assertTrue("Timed out waiting for $description", condition())
        }

        override fun close() = scenario.close()
    }

    companion object {
        private fun findPlayer(view: View): PlayerView? {
            if (view is PlayerView) return view
            if (view is ViewGroup) {
                for (index in 0 until view.childCount) {
                    findPlayer(view.getChildAt(index))?.let { return it }
                }
            }
            return null
        }
    }
}
