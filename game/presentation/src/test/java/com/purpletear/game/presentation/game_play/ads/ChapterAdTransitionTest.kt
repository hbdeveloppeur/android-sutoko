package com.purpletear.game.presentation.game_play.ads

import org.junit.Assert.*
import org.junit.Test

class ChapterAdTransitionTest {
    @Test fun rewardedCloseNavigatesAndRecordsCooldownExactlyOnce() {
        val transition = ChapterAdTransition()
        var rewards = 0
        var navigations = 0
        assertTrue(transition.begin())
        assertFalse(transition.begin())
        repeat(2) {
            transition.finish(ChapterAdResult.REWARDED, { rewards++ }, { navigations++ })
        }
        assertEquals(1, rewards)
        assertEquals(1, navigations)
        assertFalse(transition.begin())
    }

    @Test fun earlyDismissalStaysAndAllowsAnotherAttempt() {
        val transition = ChapterAdTransition()
        assertTrue(transition.begin())
        transition.finish(ChapterAdResult.DISMISSED, { fail("No reward") }, { fail("Must stay") })
        assertFalse(transition.busy)
        assertTrue(transition.begin())
        var navigated = false
        transition.finish(ChapterAdResult.REWARDED, {}, { navigated = true })
        assertTrue(navigated)
    }

    @Test fun unavailableAdContinuesWithoutStartingCooldown() {
        val transition = ChapterAdTransition()
        transition.begin()
        var navigated = false
        transition.finish(ChapterAdResult.UNAVAILABLE, { fail("No reward") }, { navigated = true })
        assertTrue(navigated)
        assertFalse(transition.busy)
    }
}
