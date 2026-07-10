package com.purpletear.game.presentation.game_play

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsGameRoutesTest {

    @Test
    fun `GAME route pattern declares the isTrial argument`() {
        // The ViewModel reads isTrial from the nav SavedStateHandle, which is populated
        // from the route URI. The arg must be part of the route pattern.
        assertTrue(SmsGameRoutes.GAME.contains(SmsGameRoutes.IS_TRIAL_ARG))
    }

    @Test
    fun `game route without trial encodes isTrial false`() {
        assertEquals(
            "game/play/1A?isLiveUpdateMode=false&isTrial=false",
            SmsGameRoutes.game(chapterCode = "1A"),
        )
    }

    @Test
    fun `game route with trial encodes isTrial true`() {
        assertEquals(
            "game/play/1A?isLiveUpdateMode=false&isTrial=true",
            SmsGameRoutes.game(chapterCode = "1A", isTrial = true),
        )
    }
}
