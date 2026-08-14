package com.purpletear.game.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.purpletear.sutoko.game.model.StoryAdvanceMode
import com.purpletear.sutoko.game.repository.StoryAdvanceModeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [StoryAdvanceModeRepository] backed by Jetpack DataStore so the mode survives app restarts.
 * Missing or unknown values mean the player never picked a mode and surface as null.
 */
@Singleton
class StoryAdvanceModeRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : StoryAdvanceModeRepository {

    override fun observeExplicit(): Flow<StoryAdvanceMode?> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences -> preferences[KEY_MODE].toStoryAdvanceMode() }

    override suspend fun set(mode: StoryAdvanceMode) {
        dataStore.edit { preferences ->
            preferences[KEY_MODE] = mode.name
        }
    }

    private fun String?.toStoryAdvanceMode(): StoryAdvanceMode? =
        StoryAdvanceMode.entries.firstOrNull { it.name == this }

    private companion object {
        val KEY_MODE = stringPreferencesKey("story_advance_mode")
    }
}
