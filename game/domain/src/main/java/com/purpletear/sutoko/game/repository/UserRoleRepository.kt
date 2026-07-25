package com.purpletear.sutoko.game.repository

import com.purpletear.sutoko.game.model.UserRole
import kotlinx.coroutines.flow.Flow

/**
 * Persists the user's game [UserRole] across app restarts.
 */
interface UserRoleRepository {
    /** Observes the current role. Always emits, defaults to [UserRole.PLAYER]. */
    fun observe(): Flow<UserRole>

    /** Returns the current role, [UserRole.PLAYER] when never set. */
    suspend fun get(): UserRole

    /** Persists [role]. */
    suspend fun set(role: UserRole)
}
