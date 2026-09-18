package com.purpletear.game.presentation.game_play

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.descendants
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.purpletear.game.presentation.game_play.components.background.VideoBackground
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VideoBackgroundTransitionTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun successiveVideoPathsRebindExistingViewAndSamePathKeepsPlayer() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val videos = (1..3).map { index ->
            File(instrumentation.targetContext.cacheDir, "background-transition-$index.mp4").apply {
                instrumentation.context.assets.open("preview-transition.mp4").use { source ->
                    outputStream().use(source::copyTo)
                }
            }
        }
        val path = mutableStateOf(videos.first().absolutePath)
        val tag = mutableStateOf("initial")
        val visible = mutableStateOf(true)
        compose.setContent {
            if (visible.value) {
                VideoBackground(path.value, Modifier.fillMaxSize().testTag(tag.value))
            }
        }
        try {
            lateinit var view: PlayerView
            lateinit var previousPlayer: Player
            compose.runOnIdle {
                view = playerView()
                previousPlayer = requireNotNull(view.player)
            }
            awaitPlayback()
            compose.runOnIdle { tag.value = "recomposed" }
            compose.runOnIdle {
                assertSame(view, playerView())
                assertSame(previousPlayer, view.player)
            }
            for (video in videos.drop(1)) {
                compose.runOnIdle { path.value = video.absolutePath }
                compose.runOnIdle {
                    assertSame(view, playerView())
                    val currentPlayer = requireNotNull(view.player)
                    assertNotSame(previousPlayer, currentPlayer)
                    assertEquals(video.toURI().path, currentPlayer.currentMediaItem?.localConfiguration?.uri?.path)
                    assertEquals(Player.STATE_IDLE, previousPlayer.playbackState)
                    assertEquals(0f, currentPlayer.volume, 0f)
                    assertEquals(Player.REPEAT_MODE_ALL, currentPlayer.repeatMode)
                    previousPlayer = currentPlayer
                }
                awaitPlayback()
            }
            compose.runOnIdle { visible.value = false }
            compose.runOnIdle { assertEquals(Player.STATE_IDLE, previousPlayer.playbackState) }
        } finally {
            compose.runOnIdle { visible.value = false }
            compose.waitForIdle()
            videos.forEach { it.delete() }
        }
    }

    private fun playerView(): PlayerView =
        (compose.activity.window.decorView as ViewGroup).descendants.filterIsInstance<PlayerView>().single()

    private fun awaitPlayback() {
        compose.waitUntil(10_000) {
            var playing = false
            compose.runOnUiThread { playing = playerView().player?.isPlaying == true }
            playing
        }
        compose.runOnIdle { assertTrue(requireNotNull(playerView().player).videoSize.width > 0) }
    }
}
