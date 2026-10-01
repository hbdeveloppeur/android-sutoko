package com.purpletear.game.presentation.game_preview.components

import com.purpletear.sutoko.game.model.Chapter
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class ChapterReleaseDateTest {
    private val originalTimeZone = TimeZone.getDefault()

    @Before
    fun setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `preview date includes year and time in French`() {
        assertEquals(
            "lundi 3 août 2026, 18:30",
            Chapter(releaseDate = RELEASE_DATE_SECONDS).formatReleaseDate(Locale.FRANCE, includeYear = true),
        )
    }

    @Test
    fun `toast date includes time without year`() {
        assertEquals(
            "lundi 3 août, 18:30",
            Chapter(releaseDate = RELEASE_DATE_SECONDS).formatReleaseDate(Locale.FRANCE),
        )
    }

    @Test
    fun `date and time preserve the requested locale`() {
        assertEquals(
            "Monday 3 August 2026, 18:30",
            formatReleaseDate(RELEASE_DATE_SECONDS, Locale.US, includeYear = true),
        )
    }

    @Test
    fun `release time uses the device time zone including summer time`() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"))
        assertEquals(
            "lundi 3 août 2026, 20:30",
            formatReleaseDate(RELEASE_DATE_SECONDS, Locale.FRANCE, includeYear = true),
        )
    }

    @Test
    fun `local date rolls over with the release time`() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
        assertEquals(
            "mardi 4 août 2026, 03:30",
            formatReleaseDate(RELEASE_DATE_SECONDS, Locale.FRANCE, includeYear = true),
        )
    }

    companion object {
        // 2026-08-03T18:30:00Z, in server epoch seconds.
        private const val RELEASE_DATE_SECONDS = 1785781800L
    }
}
