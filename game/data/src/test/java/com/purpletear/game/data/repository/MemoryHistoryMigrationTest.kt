package com.purpletear.game.data.repository

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import com.purpletear.game.data.database.GameDatabase
import com.purpletear.game.data.database.migrations.GameDatabaseMigrations
import com.purpletear.game.data.local.entity.MemoryEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class MemoryHistoryMigrationTest {
    @Test
    fun migrationPreservesStoredValuesAndEnablesChapterHistory() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val name = "memory-history-migration"
        context.deleteDatabase(name)
        try {
            // Keep the other tables current; recreate the exact v20 memory schema.
            val seed = Room.databaseBuilder(context, GameDatabase::class.java, name)
                .allowMainThreadQueries().build()
            seed.openHelper.writableDatabase
            seed.close()
            SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, 0).use { db ->
                db.execSQL("DROP TABLE game_memories")
                db.execSQL("""
                    CREATE TABLE game_memories (
                        gameId TEXT NOT NULL, `key` TEXT NOT NULL, value TEXT NOT NULL,
                        chapterNumber INTEGER NOT NULL DEFAULT 2147483647,
                        PRIMARY KEY(gameId, `key`)
                    )
                """.trimIndent())
                db.execSQL("INSERT INTO game_memories VALUES ('story', 'trust', '1', 1)")
                db.version = 20
            }
            val migrated = Room.databaseBuilder(context, GameDatabase::class.java, name)
                .addMigrations(GameDatabaseMigrations.MIGRATION_20_21)
                .allowMainThreadQueries().build()
            try {
                val dao = migrated.memoryDao()
                val original = MemoryEntity("story", "trust", "1", 1)
                assertEquals(listOf(original), dao.getAllForGameUpToChapter("story", 3))
                dao.insert(MemoryEntity("story", "trust", "2", 2))
                assertEquals(listOf(original), dao.loadBeforeChapter("story", 2))
            } finally {
                migrated.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }
}
