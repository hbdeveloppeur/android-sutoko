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
    val publishedVersion: Int = 0,
    val code: String = ""
)
