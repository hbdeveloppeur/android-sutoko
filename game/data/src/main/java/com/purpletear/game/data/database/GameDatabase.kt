package com.purpletear.game.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.purpletear.game.data.local.dao.ChapterDao
import com.purpletear.game.data.local.dao.GameDao
import com.purpletear.game.data.local.dao.GameInstallationDao
import com.purpletear.game.data.local.dao.UserGameProgressDao
import com.purpletear.game.data.local.entity.GameInstallationEntity
import com.purpletear.sutoko.game.model.Chapter
import com.purpletear.sutoko.game.model.Game
import com.purpletear.sutoko.game.model.UserGameProgressEntity

@Database(
    entities = [Chapter::class, UserGameProgressEntity::class, Game::class, GameInstallationEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(GameTypeConverters::class)
abstract class GameDatabase : RoomDatabase() {
    abstract fun chapterDao(): ChapterDao
    abstract fun gameDao(): GameDao
    abstract fun userGameProgressDao(): UserGameProgressDao
    abstract fun gameInstallationDao(): GameInstallationDao
}
