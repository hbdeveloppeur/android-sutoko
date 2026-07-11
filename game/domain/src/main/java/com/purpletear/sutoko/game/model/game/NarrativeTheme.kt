package com.purpletear.sutoko.game.model.game

import androidx.annotation.Keep

/**
 * A single narrative theme (a.k.a. genre) of a story.
 *
 * [id] is the stable theme key (e.g. "romance"); [name] is the server-localized display label
 * for the requested language (e.g. "Romance", "Romance", "Romance").
 */
@Keep
data class NarrativeTheme(
    val id: String,
    val name: String,
)
