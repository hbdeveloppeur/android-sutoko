package com.purpletear.game.data.repository.testing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.purpletear.sutoko.game.repository.testing.LastTestedChapterRepository
import com.purpletear.sutoko.game.testing.StoryTestingLogger
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [LastTestedChapterRepository] backed by Jetpack DataStore.
 *
 * Reads and writes are wrapped in `runCatching` so a corrupted preference file cannot break
 * the real-time testing session.
 */
@Singleton
class DataStoreLastTestedChapterRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : LastTestedChapterRepository {

    override suspend fun get(storyId: String): String? {
        return runCatching {
            dataStore.data
                .catch { error ->
                    if (error is IOException) {
                        StoryTestingLogger.e("PREFS", error) { "Failed to read last-tested chapter for $storyId" }
                        emit(emptyPreferences())
                    } else {
                        throw error
                    }
                }
                .map { preferences ->
                    preferences[stringPreferencesKey(keyFor(storyId))]
                }
                .first()
        }.getOrElse { error ->
            StoryTestingLogger.e("PREFS", error) { "Unexpected error reading last-tested chapter for $storyId" }
            null
        }
    }

    override suspend fun set(storyId: String, chapterId: String) {
        runCatching {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey(keyFor(storyId))] = chapterId
            }
            StoryTestingLogger.d("PREFS") { "Last-tested chapter saved — story=$storyId, chapter=$chapterId" }
        }.onFailure { error ->
            StoryTestingLogger.e("PREFS", error) { "Failed to save last-tested chapter for $storyId" }
        }
    }

    private fun keyFor(storyId: String): String = "last_tested_chapter_$storyId"
}
