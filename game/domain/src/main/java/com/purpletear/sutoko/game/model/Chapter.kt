package com.purpletear.sutoko.game.model

import androidx.annotation.Keep

@Keep
data class Chapter(
    val id: String = "",
    val number: Int = 1,
    val alternative: String = "",
    val releaseDate: Long = 0L,
    val createdAt: Long = 0L,
    val story: String = "",
    val title: String = "",
    val description: String = "",
    val canvasAppVersion: Int = 0,
    val code: String = "",
    val available: Boolean = false,
    /**
     * Ids of the characters whose messages are displayed on the right side of the
     * conversation (from the chapter `layout.sides.right` payload). Every other
     * character is displayed on the left. Empty when the chapter declares no
     * layout: callers then fall back to the legacy main-character rule.
     */
    val rightSideCharacterIds: List<Int> = emptyList(),
) {

    val normalizedCode: String
        get() = code.lowercase()
}
