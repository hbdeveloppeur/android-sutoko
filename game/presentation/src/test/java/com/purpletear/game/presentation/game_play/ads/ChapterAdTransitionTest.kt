package com.purpletear.game.presentation.game_play.ads

import org.junit.Assert.*
import org.junit.Test

class ChapterAdTransitionTest {
    @Test fun fullscreenDismissalAdvancesExactlyOnce() {
        val transition = ChapterAdTransition()
        var navigations = 0
        assertTrue(transition.begin())
        assertFalse(transition.begin())
        repeat(2) { transition.finish { navigations++ } }
        assertEquals(1, navigations)
        assertFalse(transition.begin())
        assertFalse(transition.busy)
    }
    @Test fun missingInventoryAlsoAdvancesExactlyOnce() {
        val transition = ChapterAdTransition()
        var navigations = 0
        transition.begin()
        transition.finish { navigations++ }
        transition.finish { navigations++ }
        assertEquals(1, navigations)
    }
}
