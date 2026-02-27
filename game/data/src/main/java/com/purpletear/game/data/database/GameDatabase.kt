package com.purpletear.game.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.purpletear.game.data.local.dao.ChapterDao
import com.purpletear.sutoko.game.model.Chapter

@Database(
    entities = [Chapter::class],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun chapterDao(): ChapterDao
}
