package com.purpletear.sutoko.game.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "chapters")
data class Chapter(
    @PrimaryKey
    val id: String = "",
    val number: Int = 1,
    val alternative: String = "",
    val releaseDate: Long = 0L,
    val createdAt: Long = 0L,
    val story: String = "",
    val title: String = "",
    val description: String = "",
    val canvasAppVersion: Int = 0,
    val code: String = ""
) {
    val isAvailable: Boolean
        get() = releaseDate <= System.currentTimeMillis()
}
