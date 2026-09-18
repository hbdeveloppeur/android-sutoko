package com.purpletear.game.presentation.game_play.ads

/** Serializes clicks and SDK callbacks for one chapter boundary. */
internal class ChapterAdTransition {
    var busy: Boolean = false
        private set
    private var completed = false

    fun begin(): Boolean {
        if (busy || completed) return false
        busy = true
        return true
    }

    fun finish(result: ChapterAdResult, recordReward: () -> Unit, navigate: () -> Unit) {
        if (!busy || completed) return
        busy = false
        if (result == ChapterAdResult.DISMISSED) return
        completed = true
        if (result == ChapterAdResult.REWARDED) recordReward()
        navigate()
    }
}
