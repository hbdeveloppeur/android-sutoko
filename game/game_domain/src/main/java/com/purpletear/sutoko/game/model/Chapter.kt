package com.purpletear.sutoko.game.model

import androidx.annotation.Keep

@Keep
data class Chapter(
    val id: Int = 0,
    val number: Int = 1,
    val alternative: String = "",
    val releaseDate: Long = 0L,
    val createdAt: Long = 0L,
    val minAppCode: Int = 0,
    val switchable: Boolean = false,
    val minStoryVersion: String = "",
    val title: String = "",
    val description: String = "",
    val isAvailable: Boolean = false
) {
    /**
     * Get the chapter code, which is the number and alternative (in lowercase).
     * For example, if number is 1 and alternative is "A", the code will be "1a".
     *
     * @return The chapter code as a string.
     */
    fun getCode(): String {
        return "${number}${alternative.lowercase()}"
    }
}
