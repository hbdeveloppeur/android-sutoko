package com.purpletear.game.presentation.game_play.ads

import org.junit.Assert.assertEquals
import org.junit.Test

class ChapterAdCooldownTest {
    @Test fun firstTransitionIsEligible() {
        assertEquals(0L, remainingCooldown(0L, 123L))
    }

    @Test fun cooldownExpiresAtFiveMinutes() {
        assertEquals(1L, remainingCooldown(100L, 300099L))
        assertEquals(0L, remainingCooldown(100L, 300100L))
        assertEquals(0L, remainingCooldown(100L, 400100L))
    }

    @Test fun clockRollbackDoesNotImmediatelyShowAnotherAd() {
        assertEquals(CHAPTER_AD_COOLDOWN_MS, remainingCooldown(100L, 99L))
    }
}
