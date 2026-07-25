package com.purpletear.game.presentation.game_preview.components

import com.purpletear.sutoko.game.model.Chapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Release date formatted for display, e.g. "Monday 3 August". */
internal fun Chapter.formatReleaseDate(locale: Locale = Locale.getDefault()): String =
    SimpleDateFormat("EEEE d MMMM", locale).format(Date(releaseDate * 1000))
