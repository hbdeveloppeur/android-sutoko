package com.purpletear.game.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.purpletear.game.data.local.dao.ChapterDao
import com.purpletear.game.data.local.dao.GameDao
import com.purpletear.game.data.local.dao.GameInstallationDao
import com.purpletear.game.data.local.dao.MemoryDao
import com.purpletear.game.data.local.dao.UserGameProgressDao
import com.purpletear.game.data.local.entity.ChapterEntity
import com.purpletear.game.data.local.entity.GameCatalogEntity
import com.purpletear.game.data.local.entity.GameInstallEntity
import com.purpletear.game.data.local.entity.MemoryEntity
import com.purpletear.game.data.local.entity.UserGameProgressEntity

@Database(
    entities = [
        ChapterEntity::class,
        UserGameProgressEntity::class,
        GameCatalogEntity::class,
        GameInstallEntity::class,
        MemoryEntity::class,
    ],
    version = 14,
    exportSchema = false,
)
@TypeConverters(GameTypeConverters::class)
abstract class GameDatabase : RoomDatabase() {
    abstract fun chapterDao(): ChapterDao
    abstract fun gameDao(): GameDao
    abstract fun userGameProgressDao(): UserGameProgressDao
    abstract fun gameInstallationDao(): GameInstallationDao
    abstract fun memoryDao(): MemoryDao
}
