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

    /**
     * Migration from version 10 to 11:
     * - Adds chapterNumber column to game_memories so each memory can be tagged with
     *   the chapter in which it was written.
     * - Existing rows default to Int.MAX_VALUE so they are treated as "future" state
     *   and cleaned up the next time a chapter loads, preventing overlap with replays.
     */
    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE game_memories ADD COLUMN chapterNumber INTEGER NOT NULL DEFAULT ${Int.MAX_VALUE}"
            )
        }
    }

    /**
     * Migration from version 11 to 12:
     * - Adds userNickNameRequired flag to games catalog.
     * - Existing games default to false (0).
     */
    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE games ADD COLUMN userNickNameRequired INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    /**
     * Migration from version 12 to 13:
     * - Adds narrativeThemes (JSON-encoded List<String>) to the games catalog so cards and
     *   previews can display server-localized genres.
     * - Existing games default to an empty list ("[]"), which the UI maps to the localized
     *   genre fallback string.
     */
    val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE games ADD COLUMN narrativeThemes TEXT NOT NULL DEFAULT '[]'"
            )
        }
    }

    /**
     * Migration from version 14 to 15:
     * - Adds the game_favorites table storing the user's favorite stories.
     */
    val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS game_favorites (
                    gameId TEXT PRIMARY KEY NOT NULL,
                    addedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    /**
     * Migration from version 16 to 17:
     * - Adds rightSideCharacterIds (comma-separated character ids) to chapters so the
     *   conversation screen knows which characters are displayed on the right side.
     * - Existing chapters default to an empty string, meaning "no layout": the UI then
     *   falls back to the legacy main-character rule.
     */
    val MIGRATION_16_17 = object : Migration(16, 17) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE chapters ADD COLUMN rightSideCharacterIds TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    /**
     * Migration from version 17 to 18:
     * - Adds menuSoundUrl (nullable) to the games catalog so the preview screen
     *   can play the story's menu ambience.
     */
    val MIGRATION_17_18 = object : Migration(17, 18) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE games ADD COLUMN menuSoundUrl TEXT"
            )
        }
    }

    /**
     * Migration from version 18 to 19:
     * - Adds verticalBanner (nullable, JSON-encoded Asset) and cardLayout to the games
     *   catalog so stories can be showcased as portrait posters in a horizontally
     *   scrollable row (Netflix-style). Existing games default to 'HORIZONTAL'.
     */
    val MIGRATION_18_19 = object : Migration(18, 19) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE games ADD COLUMN verticalBanner TEXT"
            )
            db.execSQL(
                "ALTER TABLE games ADD COLUMN cardLayout TEXT NOT NULL DEFAULT 'HORIZONTAL'"
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(
        MIGRATION_6_7,
        MIGRATION_10_11,
        MIGRATION_11_12,
        MIGRATION_12_13,
        MIGRATION_14_15,
        MIGRATION_16_17,
        MIGRATION_17_18,
        MIGRATION_18_19,
    )
}
