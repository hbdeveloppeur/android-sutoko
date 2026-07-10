package com.purpletear.game.presentation.common.extensions

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * JVM tests for [Color.parseOrNull].
 *
 * Note: the 6-digit `#RRGGBB` branch delegates to `android.graphics.Color.parseColor` (via
 * `androidx.core.graphics.toColorInt`), which is not available in plain JVM unit tests. The 8-digit
 * `#AARRGGBB` branch is parsed manually, so it exercises the success path here without Robolectric;
 * the 6-digit success path is covered by manual verification.
 */
class ColorExtensionsTest {

    @Test
    fun `valid 8-digit hex with alpha returns a color`() {
        val color = Color.parseOrNull("#FFFF2200")

        assertNotNull(color)
        assertEquals(Color(0xFFFF2200.toInt()), color)
    }

    @Test
    fun `valid 8-digit black is non-null and distinct from failure`() {
        val color = Color.parseOrNull("#FF000000")

        assertNotNull(color)
        assertEquals(Color(0xFF000000.toInt()), color)
    }

    @Test
    fun `null input returns null`() {
        assertNull(Color.parseOrNull(null))
    }

    @Test
    fun `blank input returns null`() {
        assertNull(Color.parseOrNull(""))
        assertNull(Color.parseOrNull("   "))
    }

    @Test
    fun `malformed hex returns null`() {
        assertNull(Color.parseOrNull("#GG2200"))
        assertNull(Color.parseOrNull("#12345"))
        assertNull(Color.parseOrNull("#1234567890"))
        assertNull(Color.parseOrNull("not-a-color"))
    }
}
