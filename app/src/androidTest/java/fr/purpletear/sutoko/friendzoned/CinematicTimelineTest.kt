package fr.purpletear.sutoko.friendzoned

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import purpletear.fr.purpleteartools.CinematicTimeline

@RunWith(AndroidJUnit4::class)
class CinematicTimelineTest {
    @Test fun followsOnlyReachableCuesAndCountsZeroDurationBranchEvents() {
        val cues = mapOf(
            1 to CinematicTimeline.Cue(1000, 2),
            2 to CinematicTimeline.Cue(0, 4),
            3 to CinematicTimeline.Cue(60000, null),
            4 to CinematicTimeline.Cue(2500, null),
        )
        assertEquals(3500L, CinematicTimeline.duration(1) { cues.getValue(it) })
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsCycles() {
        CinematicTimeline.duration(1) { CinematicTimeline.Cue(1000, 1) }
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsNegativeDurations() {
        CinematicTimeline.duration(1) { CinematicTimeline.Cue(-1, null) }
    }
}
