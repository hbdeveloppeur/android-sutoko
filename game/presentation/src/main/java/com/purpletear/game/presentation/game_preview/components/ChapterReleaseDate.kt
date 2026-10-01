package com.purpletear.game.presentation.game_preview.components

import com.purpletear.sutoko.game.model.Chapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Release date and time in the device time zone, with an optional year. */
internal fun Chapter.formatReleaseDate(locale: Locale = Locale.getDefault(), includeYear: Boolean = false): String =
    formatReleaseDate(releaseDate, locale, includeYear)

/** Formats an epoch-second release date with local time, e.g. "Monday 3 August 2026, 18:30". */
internal fun formatReleaseDate(releaseDateSeconds: Long, locale: Locale = Locale.getDefault(), includeYear: Boolean = false): String =
    SimpleDateFormat(if (includeYear) "EEEE d MMMM yyyy, HH:mm" else "EEEE d MMMM, HH:mm", locale)
        .format(Date(releaseDateSeconds * 1000))
