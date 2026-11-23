package com.purpletear.game.data.repository

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import com.purpletear.game.data.database.GameDatabase
import com.purpletear.game.data.database.migrations.GameDatabaseMigrations
import com.purpletear.game.data.local.entity.MemoryEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest=Config.NONE,sdk=[28])
class GameCheckpointMigrationTest {
    @Test fun version21UpgradeAddsCheckpointsWithoutClearingOldProgressOrChoices() = runBlocking {
        val context=RuntimeEnvironment.getApplication();val name="checkpoint-migration"
        context.deleteDatabase(name)
        try {
            val seed=Room.databaseBuilder(context,GameDatabase::class.java,name).allowMainThreadQueries().build()
            seed.memoryDao().insert(MemoryEntity("story","IStayed","true",8));seed.close()
            SQLiteDatabase.openDatabase(context.getDatabasePath(name).path,null,0).use { db ->
                db.execSQL("DROP TABLE user_game_progress")
                db.execSQL("CREATE TABLE user_game_progress (gameId TEXT NOT NULL PRIMARY KEY,currentChapterCode TEXT NOT NULL,normalizedChapterCode TEXT NOT NULL,heroName TEXT NOT NULL)")
                db.execSQL("INSERT INTO user_game_progress VALUES ('story','9B','9b','Alex')")
                db.version=21
            }
            val db=Room.databaseBuilder(context,GameDatabase::class.java,name)
                .addMigrations(GameDatabaseMigrations.MIGRATION_21_22).allowMainThreadQueries().build()
            try {
                val progress=UserGameProgressRepositoryImpl(db.userGameProgressDao()).get("story")
                assertEquals("9B",progress.currentChapterCode);assertEquals("Alex",progress.heroName)
                assertNull(progress.checkpoint)
                assertEquals("true",db.memoryDao().getAllForGameUpToChapter("story",9).single().value)
            } finally {db.close()}
        } finally {context.deleteDatabase(name)}
    }
}
