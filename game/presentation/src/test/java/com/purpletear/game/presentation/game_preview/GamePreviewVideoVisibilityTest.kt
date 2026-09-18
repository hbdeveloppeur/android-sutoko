package com.purpletear.game.presentation.game_preview

import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GamePreviewVideoVisibilityTest {
    @Test
    fun `navigation pauses the attached player and resumes without another entrance delay`() = runTest {
        val lifecycle = MutableStateFlow(Lifecycle.State.STARTED)
        var playback = PreviewVideoPlaybackState()
        backgroundScope.launch { previewVideoPlayback(lifecycle).collect { playback = it } }
        runCurrent()
        assertFalse(playback.attachPlayer)

        lifecycle.value = Lifecycle.State.RESUMED
        runCurrent()
        assertTrue(playback.attachPlayer)
        assertTrue(playback.playWhenReady)
        assertEquals(0L, testScheduler.currentTime)

        lifecycle.value = Lifecycle.State.STARTED
        runCurrent()
        assertTrue(playback.attachPlayer)
        assertFalse(playback.playWhenReady)

        lifecycle.value = Lifecycle.State.CREATED
        runCurrent()
        assertTrue(playback.attachPlayer)
        assertFalse(playback.playWhenReady)

        lifecycle.value = Lifecycle.State.RESUMED
        runCurrent()
        assertTrue(playback.attachPlayer)
        assertTrue(playback.playWhenReady)
        assertEquals(0L, testScheduler.currentTime)
    }

    @Test
    fun `leaving before entrance finishes never allocates a player`() = runTest {
        val lifecycle = MutableStateFlow(Lifecycle.State.STARTED)
        val observed = mutableListOf<PreviewVideoPlaybackState>()
        backgroundScope.launch { previewVideoPlayback(lifecycle).collect { observed += it } }
        runCurrent()

        lifecycle.value = Lifecycle.State.CREATED
        runCurrent()
        lifecycle.value = Lifecycle.State.DESTROYED
        runCurrent()

        assertEquals(listOf(PreviewVideoPlaybackState()), observed)
    }
}
