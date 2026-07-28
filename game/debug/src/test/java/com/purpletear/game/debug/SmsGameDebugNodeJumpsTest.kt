package com.purpletear.game.debug

import org.junit.Assert.assertNull
import org.junit.Test

class SmsGameDebugNodeJumpsTest {

    @Test
    fun `returns null for unknown chapter code`() {
        assertNull(SmsGameDebugNodeJumps.getNodeId("unknown"))
    }
}
