package com.purpletear.ntfy

import org.junit.Test
import org.junit.Assert.*

class NtfyClientTest {

    @Test
    fun `NtfyConfig creation is correct`() {
        val config = NtfyConfig(
            errorChannelId = "error-channel",
            logChannelId = "log-channel",
            urgentChannelId = "urgent-channel",
            baseUrl = "https://custom.ntfy.sh",
            silent = true
        )

        assertEquals("error-channel", config.errorChannelId)
        assertEquals("log-channel", config.logChannelId)
        assertEquals("urgent-channel", config.urgentChannelId)
        assertEquals("https://custom.ntfy.sh", config.baseUrl)
        assertTrue(config.silent)
    }

    @Test
    fun `NtfyConfig has correct defaults`() {
        val config = NtfyConfig()

        assertEquals("", config.errorChannelId)
        assertEquals("", config.logChannelId)
        assertEquals("", config.urgentChannelId)
        assertEquals("https://ntfy.sh", config.baseUrl)
        assertFalse(config.silent)
    }

    @Test(expected = NtfyException::class)
    fun `NtfyException can be thrown`() {
        throw NtfyException("Test error message")
    }

    @Test
    fun `NtfyException has correct message`() {
        val exception = NtfyException("Test message")
        assertEquals("Test message", exception.message)
    }

    @Test
    fun `NtfyException with cause`() {
        val cause = RuntimeException("Original error")
        val exception = NtfyException("Wrapped error", cause)
        assertEquals("Wrapped error", exception.message)
        assertEquals(cause, exception.cause)
    }
}
