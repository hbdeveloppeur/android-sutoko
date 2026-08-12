package com.purpletear.game.presentation.game_play.pacing

import com.purpletear.game.data.infrastructure.SystemTimingScheduler

/**
 * Combines the two manual pacing freezes (finger held on the screen, fullscreen image
 * viewer) into the timing scheduler's hold-pause flag. The freeze happens inside the
 * scheduler, so in-flight scripts are never dropped and an in-flight delay keeps its
 * remaining time instead of finishing behind the viewer. The two pause sources are
 * combined, so lifting the finger does not resume the story while the viewer is open.
 */
class TimingGate(
    private val timingScheduler: SystemTimingScheduler,
    private val onPausedChanged: (Boolean) -> Unit,
) {
    var isFingerHeld = false
        private set
    var isImageViewerOpen = false
        private set

    fun setFingerHeld(held: Boolean) {
        if (isFingerHeld == held) return
        isFingerHeld = held
        apply()
    }

    fun setImageViewerOpen(open: Boolean) {
        if (isImageViewerOpen == open) return
        isImageViewerOpen = open
        apply()
    }

    /**
     * Clears both sources and unpauses. The scheduler is a process-wide singleton:
     * never inherit a stale hold from a previous session.
     */
    fun reset() {
        isFingerHeld = false
        isImageViewerOpen = false
        apply()
    }

    private fun apply() {
        val paused = isFingerHeld || isImageViewerOpen
        timingScheduler.setHoldPaused(paused)
        onPausedChanged(paused)
    }
}
