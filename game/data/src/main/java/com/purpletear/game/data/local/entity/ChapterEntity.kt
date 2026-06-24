package com.purpletear.game.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.purpletear.sutoko.game.model.Chapter

@Keep
@Entity(tableName = "chapters")
data class ChapterEntity(
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
    val code: String = "",
    val available: Boolean = false,
)

fun ChapterEntity.toDomain(): Chapter = Chapter(
    id = id,
    number = number,
    alternative = alternative,
    releaseDate = releaseDate,
    createdAt = createdAt,
    story = story,
    title = title,
    description = description,
    canvasAppVersion = canvasAppVersion,
    code = code.uppercase(),
)

fun Chapter.toEntity(): ChapterEntity = ChapterEntity(
    id = id,
    number = number,
    alternative = alternative,
    releaseDate = releaseDate,
    createdAt = createdAt,
    story = story,
    title = title,
    description = description,
    canvasAppVersion = canvasAppVersion,
    code = code.uppercase(),
)
