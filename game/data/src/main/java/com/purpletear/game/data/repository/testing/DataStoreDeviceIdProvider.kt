package com.purpletear.game.data.repository.testing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.purpletear.sutoko.game.repository.testing.DeviceIdProvider
import com.purpletear.sutoko.game.testing.StoryTestingLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [DeviceIdProvider] backed by Jetpack DataStore.
 *
 * Generates a random UUID on first access, persists it, and caches it in memory.
 * If DataStore is unreadable, a new UUID is generated and used for the current process.
 */
@Singleton
class DataStoreDeviceIdProvider @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : DeviceIdProvider {

    private val mutex = Mutex()
    private var cachedDeviceId: String? = null

    override suspend fun get(): String {
        cachedDeviceId?.let { return it }

        return mutex.withLock {
            cachedDeviceId?.let { return@withLock it }

            val storedId = readDeviceId()
            val deviceId = if (!storedId.isNullOrBlank()) {
                storedId
            } else {
                val generated = UUID.randomUUID().toString()
                writeDeviceId(generated)
                generated
            }
            cachedDeviceId = deviceId
            deviceId
        }
    }

    private suspend fun readDeviceId(): String? {
        return try {
            dataStore.data
                .catch { error ->
                    if (error is IOException) {
                        StoryTestingLogger.e("PREFS", error) { "Failed to read device id" }
                        emit(emptyPreferences())
                    } else {
                        throw error
                    }
                }
                .map { preferences -> preferences[DEVICE_ID_KEY] }
                .first()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            StoryTestingLogger.e("PREFS", e) { "Unexpected error reading device id" }
            null
        }
    }

    private suspend fun writeDeviceId(deviceId: String) {
        try {
            dataStore.edit { preferences ->
                preferences[DEVICE_ID_KEY] = deviceId
            }
            StoryTestingLogger.d("PREFS") { "Device id saved" }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            StoryTestingLogger.e("PREFS", e) { "Failed to save device id" }
        }
    }

    companion object {
        private val DEVICE_ID_KEY = stringPreferencesKey("device_id")
    }
}
