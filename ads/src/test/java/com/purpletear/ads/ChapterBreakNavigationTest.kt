package com.purpletear.ads

import org.junit.Assert.*
import org.junit.Test

class ChapterBreakNavigationTest {
    @Test fun resumeWithoutChildResultCannotLaunchTwice() {
        val gate = ChapterBreakNavigation()
        assertTrue(gate.launch())
        assertFalse(gate.launch())
        gate.childReturned(null, false)
        assertTrue(gate.launch())
    }
    @Test fun cinematicReturnHasNoAdCandidate() {
        val gate = ChapterBreakNavigation(childLaunched = true)
        gate.childReturned(null, true)
        assertNull(gate.candidate)
        assertTrue(gate.launch())
    }
    @Test fun duplicateTransitionTokenIsNotAdvertisedAgain() {
        val gate = ChapterBreakNavigation()
        gate.childReturned("event", true)
        assertTrue(gate.begin())
        assertFalse(gate.launch())
        gate.finish()
        assertTrue(gate.launch())
        gate.childReturned("event", true)
        assertNull(gate.candidate)
    }
    @Test fun restorationBypassesAnAlreadyConsumedAd() {
        val gate = ChapterBreakNavigation(consumedToken = "event")
        gate.childReturned("event", true)
        assertNull(gate.candidate)
        assertTrue(gate.launch())
    }
    @Test fun firstChapterAndUnknownGamesAreExcluded() {
        for (game in listOf(162, 161, 159, 163)) assertFalse(ChapterBreakGate.candidateChapter(game, 1))
        assertFalse(ChapterBreakGate.candidateChapter(999, 4))
        assertTrue(ChapterBreakGate.candidateChapter(162, 5))
        assertTrue(ChapterBreakGate.candidateChapter(163, 4))
    }
}
