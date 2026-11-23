package com.purpletear.game.presentation.game_play.ads

/** A chapter advances once regardless of whether a preloaded interstitial can show. */
internal class ChapterAdTransition {
    var busy = false
        private set
    private var completed = false
    fun begin(): Boolean {
        if (busy || completed) return false
        busy = true
        return true
    }
    fun finish(navigate: () -> Unit) {
        if (!busy || completed) return
        busy = false
        completed = true
        navigate()
    }
}
