package com.purpletear.game.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.purpletear.game.data.local.dao.ChapterDao
import com.purpletear.game.data.local.dao.UserGameProgressDao
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.UserGameProgressEntity

@Database(
    entities = [Chapter::class, UserGameProgressEntity::class],
    version = 2,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun chapterDao(): ChapterDao
    abstract fun userGameProgressDao(): UserGameProgressDao
}
