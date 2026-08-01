package com.purpletear.sutoko.game.model.game

import androidx.annotation.Keep

/**
 * How a story is displayed in the home feed.
 *
 * - [HORIZONTAL]: classic full-width banner card (default).
 * - [VERTICAL]: portrait poster inside a horizontally scrollable row (Netflix-style).
 */
@Keep
enum class CardLayout {
    HORIZONTAL,
    VERTICAL;

    companion object {
        /** Unknown or missing server values must never break the feed. */
        fun fromRaw(raw: String?): CardLayout =
            entries.firstOrNull { it.name == raw } ?: HORIZONTAL
    }
}
