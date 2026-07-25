package com.purpletear.game.presentation.game_preview.fakes

import com.purpletear.sutoko.game.model.UserRole
import com.purpletear.sutoko.game.repository.UserRoleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserRoleRepository : UserRoleRepository {
    private val role = MutableStateFlow(UserRole.PLAYER)

    override fun observe(): Flow<UserRole> = role

    override suspend fun get(): UserRole = role.value

    override suspend fun set(role: UserRole) {
        this.role.value = role
    }
}
