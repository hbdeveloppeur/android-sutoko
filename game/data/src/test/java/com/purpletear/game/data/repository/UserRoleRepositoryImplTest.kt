package com.purpletear.game.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.model.UserRole
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UserRoleRepositoryImplTest {
    @Test
    fun `legacy admin cannot promote an ordinary user`() = runTest {
        val store = MemoryStore(preferencesOf(stringPreferencesKey("user_role") to "ADMINISTRATOR"))
        val users = Users(User("ordinary", "token"))
        val repository = UserRoleRepositoryImpl(store, users)
        assertEquals(UserRole.PLAYER, repository.get())
        repository.set(UserRole.ADMINISTRATOR)
        assertEquals(UserRole.PLAYER, repository.get())
        assertEquals(null, store.data.value[stringPreferencesKey("user_role_owner")])
    }

    @Test
    fun `tester must opt in again and role is revoked on identity change`() = runTest {
        val store = MemoryStore(preferencesOf(stringPreferencesKey("user_role") to "ADMINISTRATOR"))
        val users = Users(User("8be954c7a18f4e7cba9c", "token"))
        val repository = UserRoleRepositoryImpl(store, users)
        val roles = mutableListOf<UserRole>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observe().collect { roles.add(it) }
        }
        assertEquals(UserRole.PLAYER, repository.get())
        repository.set(UserRole.ADMINISTRATOR)
        assertEquals(UserRole.ADMINISTRATOR, repository.get())
        users.disconnect()
        assertEquals(UserRole.PLAYER, repository.get())
        users.connect("ordinary", "token")
        repository.set(UserRole.ADMINISTRATOR)
        assertEquals(UserRole.PLAYER, repository.get())
        assertEquals(UserRole.PLAYER, roles.last())
        users.connect("8be954c7a18f4e7cba9c", "token")
        assertEquals(UserRole.ADMINISTRATOR, repository.get())
        repository.set(UserRole.PLAYER)
        assertEquals(UserRole.PLAYER, repository.get())
    }

    @Test
    fun `tester uid without token cannot activate admin`() = runTest {
        val store = MemoryStore(preferencesOf())
        val users = Users(User("8be954c7a18f4e7cba9c", ""))
        val repository = UserRoleRepositoryImpl(store, users)
        repository.set(UserRole.ADMINISTRATOR)
        assertEquals(UserRole.PLAYER, repository.get())
    }

    private class MemoryStore(initial: Preferences) : DataStore<Preferences> {
        override val data = MutableStateFlow(initial)
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            transform(data.value).also { data.value = it }
    }

    private class Users(initial: User?) : UserRepository {
        val user = MutableStateFlow(initial)
        override fun observeUser() = user
        override fun observeIsConnected() = user.map { it != null }
        override fun isConnected() = Result.success(user.value != null)
        override suspend fun connect(id: String, token: String): Result<Unit> {
            user.value = User(id, token)
            return Result.success(Unit)
        }
        override suspend fun disconnect(): Result<Unit> {
            user.value = null
            return Result.success(Unit)
        }
    }
}
