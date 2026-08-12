package com.purpletear.game.presentation.game_play.preferences

import android.content.Context

/** Persists the player's choice-box theme preference. */
class ChoicesDarkModeStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    fun read(): Boolean = preferences.getBoolean(KEY_DARK_MODE, DEFAULT_DARK_MODE)

    fun write(isDarkMode: Boolean) {
        preferences.edit()
            .putBoolean(KEY_DARK_MODE, isDarkMode)
            .apply()
    }

    private companion object {
        const val PREFS_FILE = "SUTOKO_CHOICES_DARK_MODE"
        const val KEY_DARK_MODE = "enabled"
        const val DEFAULT_DARK_MODE = true
    }
}
