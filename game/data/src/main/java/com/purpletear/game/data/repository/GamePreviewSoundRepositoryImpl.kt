package com.purpletear.game.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.purpletear.sutoko.game.repository.GamePreviewSoundRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [GamePreviewSoundRepository] backed by Jetpack DataStore so the preference
 * survives app restarts. Missing values default to unmuted.
 */
@Singleton
class GamePreviewSoundRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : GamePreviewSoundRepository {

    override fun observeMuted(): Flow<Boolean> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences -> preferences[KEY_MUTED] ?: false }

    override suspend fun setMuted(muted: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_MUTED] = muted
        }
    }

    private companion object {
        val KEY_MUTED = booleanPreferencesKey("game_preview_sound_muted")
    }
}
