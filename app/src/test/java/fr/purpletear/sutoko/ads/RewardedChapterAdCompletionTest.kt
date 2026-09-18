package fr.purpletear.sutoko.ads

import com.purpletear.game.presentation.game_play.ads.ChapterAdResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardedChapterAdCompletionTest {
    @Test
    fun rewardWaitsForDismissalAndCompletesOnce() {
        val results = mutableListOf<ChapterAdResult>()
        val completion = RewardedChapterAdCompletion(results::add)
        assertTrue(completion.onReward())
        assertTrue(results.isEmpty())
        assertFalse(completion.onReward())
        completion.onDismissed()
        completion.onDismissed()
        completion.onUnavailable()
        assertEquals(listOf(ChapterAdResult.REWARDED), results)
    }

    @Test
    fun earlyDismissalDoesNotUnlockEvenIfALateRewardArrives() {
        val results = mutableListOf<ChapterAdResult>()
        val completion = RewardedChapterAdCompletion(results::add)
        completion.onDismissed()
        assertFalse(completion.onReward())
        completion.onDismissed()
        assertEquals(listOf(ChapterAdResult.DISMISSED), results)
    }

    @Test
    fun showFailureCompletesOnceAndDoesNotGrantReward() {
        val results = mutableListOf<ChapterAdResult>()
        val completion = RewardedChapterAdCompletion(results::add)
        completion.onUnavailable()
        assertFalse(completion.onReward())
        completion.onDismissed()
        assertEquals(listOf(ChapterAdResult.UNAVAILABLE), results)
    }
}
