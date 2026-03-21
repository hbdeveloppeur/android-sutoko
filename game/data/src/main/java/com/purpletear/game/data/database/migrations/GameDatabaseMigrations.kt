package com.purpletear.game.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Registry of all GameDatabase migrations.
 * Ordered chronologically.
 */
object GameDatabaseMigrations {

    /**
     * Migration from version 6 to 7:
     * - Replaces currentChapterNumber (Int) and currentAlternative (String) 
     *   with currentChapterCode (String) and normalizedChapterCode (String)
     */
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create new table with updated schema (no DEFAULT clauses - Room expects undefined defaults)
            db.execSQL(
                """
                CREATE TABLE user_game_progress_new (
                    gameId TEXT PRIMARY KEY NOT NULL,
                    currentChapterCode TEXT NOT NULL,
                    normalizedChapterCode TEXT NOT NULL,
                    heroName TEXT NOT NULL
                )
                """.trimIndent()
            )

            // Migrate data: concatenate chapter number and alternative
            db.execSQL(
                """
                INSERT INTO user_game_progress_new (gameId, currentChapterCode, normalizedChapterCode, heroName)
                SELECT 
                    gameId,
                    CAST(currentChapterNumber AS TEXT) || currentAlternative,
                    LOWER(CAST(currentChapterNumber AS TEXT) || currentAlternative),
                    heroName
                FROM user_game_progress
                """.trimIndent()
            )

            // Drop old table
            db.execSQL("DROP TABLE user_game_progress")

            // Rename new table
            db.execSQL("ALTER TABLE user_game_progress_new RENAME TO user_game_progress")

            // Create index for normalizedChapterCode lookups
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_user_game_progress_normalized ON user_game_progress(normalizedChapterCode)"
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(
        MIGRATION_6_7
    )
}
