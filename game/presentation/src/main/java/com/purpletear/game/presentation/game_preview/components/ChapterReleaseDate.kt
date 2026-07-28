package com.purpletear.game.presentation.game_preview.components

import com.purpletear.sutoko.game.model.Chapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Release date formatted for display, e.g. "Monday 3 August" or "Monday 3 August 2026" when [includeYear] is true. */
internal fun Chapter.formatReleaseDate(locale: Locale = Locale.getDefault(), includeYear: Boolean = false): String =
    formatReleaseDate(releaseDate, locale, includeYear)

/** Formats a release date (epoch seconds) for display, e.g. "Monday 3 August" or "Monday 3 August 2026" when [includeYear] is true. */
internal fun formatReleaseDate(releaseDateSeconds: Long, locale: Locale = Locale.getDefault(), includeYear: Boolean = false): String =
    SimpleDateFormat(if (includeYear) "EEEE d MMMM yyyy" else "EEEE d MMMM", locale)
        .format(Date(releaseDateSeconds * 1000))
