package com.purpletear.sutoko.game.model

import org.junit.Assert.assertEquals
import org.junit.Test

class StoryAdvanceModeTest {

    @Test
    fun `default is click to advance for official stories`() {
        assertEquals(StoryAdvanceMode.CLICK_TO_ADVANCE, StoryAdvanceMode.defaultFor(isOfficial = true))
    }

    @Test
    fun `default is auto play for user stories`() {
        assertEquals(StoryAdvanceMode.AUTO_PLAY, StoryAdvanceMode.defaultFor(isOfficial = false))
    }
}
