package com.purpletear.sutoko.game.testing

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class StoryTestingLoggerTest {

    @Test
    fun `debug log includes category and message`() {
        val output = captureStdout {
            StoryTestingLogger.d("SESS") { "hello session" }
        }

        assertTrue("expected StoryTesting prefix", output.contains("StoryTesting"))
        assertTrue("expected [SESS] category", output.contains("[SESS]"))
        assertTrue("expected message", output.contains("hello session"))
    }

    @Test
    fun `error log includes throwable stack trace`() {
        val cause = RuntimeException("boom")
        val output = captureStdout {
            StoryTestingLogger.e("PKG", cause) { "download failed" }
        }

        assertTrue("expected [PKG] category", output.contains("[PKG ]"))
        assertTrue("expected message", output.contains("download failed"))
        assertTrue("expected stack trace", output.contains("boom"))
    }

    @Test
    fun `warning log uses yellow color marker`() {
        val output = captureStdout {
            StoryTestingLogger.w("NET") { "reconnecting" }
        }

        assertTrue("expected [NET] category", output.contains("[NET ]"))
        assertTrue("expected message", output.contains("reconnecting"))
        assertTrue("expected yellow escape code", output.contains("\u001B[33m"))
    }

    private fun captureStdout(block: () -> Unit): String {
        val original = System.out
        val stream = ByteArrayOutputStream()
        System.setOut(PrintStream(stream, true))
        try {
            block()
        } finally {
            System.setOut(original)
        }
        return stream.toString()
    }
}
