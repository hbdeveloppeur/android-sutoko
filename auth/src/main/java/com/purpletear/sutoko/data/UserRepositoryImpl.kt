package com.purpletear.sutoko.data

import android.content.SharedPreferences
import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.domain.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val prefs: SharedPreferences,
) : UserRepository {

    override fun observeUser(): Flow<User?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_ID || key == KEY_TOKEN) {
                trySend(readUser())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(readUser())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override fun observeIsConnected(): Flow<Boolean> =
        observeUser().map { it != null }

    override fun isConnected(): Result<Boolean> {
        return try {
            Result.success(readUser() != null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun connect(id: String, token: String): Result<Unit> {
        if (id.isBlank() || token.isBlank()) {
            return Result.failure(IllegalArgumentException("id and token must not be blank"))
        }
        return try {
            val success = prefs.edit()
                .putString(KEY_ID, id)
                .putString(KEY_TOKEN, token)
                .commit()
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("Failed to persist user"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun disconnect(): Result<Unit> {
        return try {
            val success = prefs.edit()
                .remove(KEY_ID)
                .remove(KEY_TOKEN)
                .commit()
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("Failed to clear user"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun readUser(): User? {
        val id = prefs.getString(KEY_ID, null)
        val token = prefs.getString(KEY_TOKEN, null)
        return if (id != null && token != null) User(id, token) else null
    }

    companion object {
        private const val KEY_ID = "KEY_UID"
        private const val KEY_TOKEN = "KEY_TOKEN"
    }
}
