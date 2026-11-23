package com.purpletear.game.presentation.game_play

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal const val GAME_FADE_DURATION_MS = 500

internal class GameFadeTransition {
    private val overlayAlpha = Animatable(1f)
    private val mutex = Mutex()
    private var isRevealing by mutableStateOf(false)
    private var isChangingContent by mutableStateOf(false)

    val alpha: Float get() = overlayAlpha.value
    val blocksInput: Boolean get() = isRevealing || isChangingContent || alpha > 0f

    suspend fun reveal() {
        if (!mutex.tryLock()) return
        isRevealing = true
        try {
            fadeTo(0f)
        } finally {
            isRevealing = false
            mutex.unlock()
        }
    }

    suspend fun fadeThroughBlack(changeContent: suspend () -> Boolean) {
        if (isChangingContent) return
        isChangingContent = true
        try {
            // Automatic navigation can arrive during the initial reveal; don't drop it.
            mutex.withLock {
                fadeTo(1f)
                if (changeContent()) fadeTo(0f)
            }
        } finally {
            isChangingContent = false
        }
    }

    private suspend fun fadeTo(alpha: Float) {
        if (alpha == overlayAlpha.value) return
        // Let the ready destination draw beneath black before revealing it.
        if (alpha == 0f) withFrameNanos { }
        overlayAlpha.animateTo(alpha, tween(GAME_FADE_DURATION_MS, easing = FastOutSlowInEasing))
    }
}
