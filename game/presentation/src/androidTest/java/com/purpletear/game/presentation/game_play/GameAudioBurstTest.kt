package com.purpletear.game.presentation.game_play

import androidx.test.platform.app.InstrumentationRegistry
import com.purpletear.game.presentation.R
import com.purpletear.game.presentation.game_play.audio.GameAudioController
import com.purpletear.game.presentation.game_play.audio.VocalPlayback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.assertEquals
import org.junit.Test

class GameAudioBurstTest {
    @Test
    fun rapidReplacementAndReleaseDuringPreparationLeavesPlaybackStopped() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val clip = java.io.File.createTempFile("typing-burst", ".mp3", context.cacheDir)
        context.resources.openRawResource(R.raw.game_presentation_typing).use { input ->
            clip.outputStream().use { input.copyTo(it) }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val audio = GameAudioController(context, scope)
        try {
            instrumentation.runOnMainSync {
                repeat(30) {
                    audio.playTypingSound()
                    audio.playSound("sound", clip.absolutePath, loop = false, volume = 0f)
                    audio.playVocal(clip.absolutePath)
                }
                audio.playSound("second", clip.absolutePath, loop = false, volume = 0f)
                audio.stopSound("sound")
                audio.stopSound("second")
                audio.playSound("delayed-a", clip.absolutePath, loop = false, volume = 0f, delayMs = 1000)
                audio.playSound("delayed-b", clip.absolutePath, loop = false, volume = 0f, delayMs = 1000)
                audio.releaseAll()
            }
            // Allow asynchronous preparation callbacks to arrive after teardown.
            android.os.SystemClock.sleep(500)
            instrumentation.runOnMainSync {
                assertEquals(VocalPlayback(), audio.vocal.value)
            }
        } finally {
            instrumentation.runOnMainSync {
                audio.releaseAll()
                scope.cancel()
            }
            clip.delete()
        }
    }
}
