package com.purpletear.game.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.model.canAccessGameOptions
import com.purpletear.sutoko.game.model.UserRole
import com.purpletear.sutoko.game.repository.UserRoleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Only the tester account can activate an account-bound administrator preference.
 * Legacy unscoped roles never grant administrator access.
 */
@Singleton
class UserRoleRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val userRepository: UserRepository,
) : UserRoleRepository {

    override fun observe(): Flow<UserRole> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .combine(userRepository.observeUser()) { preferences, user ->
            if (user.canAccessGameOptions() && preferences[KEY_ROLE_OWNER] == user?.id) {
                preferences[KEY_ROLE].toRole()
            } else {
                UserRole.PLAYER
            }
        }

    override suspend fun get(): UserRole = observe().first()

    override suspend fun set(role: UserRole) {
        val user = userRepository.observeUser().first()
        if (!user.canAccessGameOptions()) return
        dataStore.edit { preferences ->
            preferences[KEY_ROLE_OWNER] = requireNotNull(user).id
            preferences[KEY_ROLE] = role.name
        }
    }

    private fun String?.toRole(): UserRole =
        UserRole.entries.firstOrNull { it.name == this } ?: UserRole.PLAYER

    private companion object {
        val KEY_ROLE_OWNER = stringPreferencesKey("user_role_owner")
        val KEY_ROLE = stringPreferencesKey("user_role")
    }
}
