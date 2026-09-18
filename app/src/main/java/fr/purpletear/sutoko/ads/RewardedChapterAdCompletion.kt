package fr.purpletear.sutoko.ads

import com.purpletear.game.presentation.game_play.ads.ChapterAdResult

/** Google rewarded ads deliver the reward callback before dismissal. */
internal class RewardedChapterAdCompletion(
    private val onResult: (ChapterAdResult) -> Unit,
) {
    private var rewarded = false
    private var completed = false

    fun onReward(): Boolean {
        if (completed || rewarded) return false
        rewarded = true
        return true
    }

    fun onDismissed() = complete(
        if (rewarded) ChapterAdResult.REWARDED else ChapterAdResult.DISMISSED
    )

    fun onUnavailable() = complete(ChapterAdResult.UNAVAILABLE)

    private fun complete(result: ChapterAdResult) {
        if (completed) return
        completed = true
        onResult(result)
    }
}
