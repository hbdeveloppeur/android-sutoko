package com.purpletear.game.debug

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SmsGameDebugNodeJumpsTest {

    @Test
    fun `returns node id for lowercased chapter code`() {
        assertEquals("0AZY0NtFQKu-1A-215", SmsGameDebugNodeJumps.getNodeId("1A"))
        assertEquals("0AZY0NtFQKu-1A-215", SmsGameDebugNodeJumps.getNodeId("1a"))
    }

    @Test
    fun `returns null for unknown chapter code`() {
        assertNull(SmsGameDebugNodeJumps.getNodeId("unknown"))
    }
}
