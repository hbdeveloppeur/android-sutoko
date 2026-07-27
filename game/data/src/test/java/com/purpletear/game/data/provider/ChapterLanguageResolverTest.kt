package com.purpletear.game.data.provider

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChapterLanguageResolverTest {

    private val supported = listOf("fr-FR", "es-ES", "es-419", "de-DE", "en-GB", "en-US")

    @Test
    fun `exact match is returned as-is`() {
        assertEquals("es-419", ChapterLanguageResolver.resolve("es-419", supported))
        assertEquals("en-US", ChapterLanguageResolver.resolve("en-US", supported))
    }

    @Test
    fun `french locale with unsupported region falls back to fr-FR`() {
        assertEquals("fr-FR", ChapterLanguageResolver.resolve("fr-ES", supported))
        assertEquals("fr-FR", ChapterLanguageResolver.resolve("fr-CA", supported))
        assertEquals("fr-FR", ChapterLanguageResolver.resolve("fr", supported))
    }

    @Test
    fun `spanish locale with unsupported region falls back to es-ES`() {
        assertEquals("es-ES", ChapterLanguageResolver.resolve("es-MX", supported))
    }

    @Test
    fun `german and english locales fall back to canonical directories`() {
        assertEquals("de-DE", ChapterLanguageResolver.resolve("de-AT", supported))
        assertEquals("en-GB", ChapterLanguageResolver.resolve("en-AU", supported))
    }

    @Test
    fun `unknown language falls back to en-US then en-GB`() {
        assertEquals("en-US", ChapterLanguageResolver.resolve("ja-JP", supported))
        assertEquals("en-GB", ChapterLanguageResolver.resolve("ja-JP", supported - "en-US"))
        assertEquals(
            "fr-FR",
            ChapterLanguageResolver.resolve("ja-JP", supported - listOf("en-US", "en-GB"))
        )
    }

    @Test
    fun `only available languages are considered`() {
        val onlyEnglishGb = listOf("en-GB")
        assertEquals("en-GB", ChapterLanguageResolver.resolve("en-US", onlyEnglishGb))
        assertEquals("en-GB", ChapterLanguageResolver.resolve("fr-ES", onlyEnglishGb))
    }

    @Test
    fun `empty available list returns null`() {
        assertNull(ChapterLanguageResolver.resolve("fr-FR", emptyList()))
    }
}
