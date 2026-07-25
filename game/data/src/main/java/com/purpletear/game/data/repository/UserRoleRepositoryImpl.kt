package com.purpletear.game.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.purpletear.sutoko.game.model.UserRole
import com.purpletear.sutoko.game.repository.UserRoleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [UserRoleRepository] backed by Jetpack DataStore so the role survives app restarts.
 * Unknown or missing values fall back to [UserRole.PLAYER].
 */
@Singleton
class UserRoleRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserRoleRepository {

    override fun observe(): Flow<UserRole> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences -> preferences[KEY_ROLE].toRole() }

    override suspend fun get(): UserRole = observe().first()

    override suspend fun set(role: UserRole) {
        dataStore.edit { preferences ->
            preferences[KEY_ROLE] = role.name
        }
    }

    private fun String?.toRole(): UserRole =
        UserRole.entries.firstOrNull { it.name == this } ?: UserRole.PLAYER

    private companion object {
        val KEY_ROLE = stringPreferencesKey("user_role")
    }
}
