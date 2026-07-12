package com.purpletear.sutoko.shop.domain.usecase

import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.shop.test.FakeCoinPurchaseRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsStoryGrantedUseCaseTest {

    @Test
    fun `invoke returns false when user is not connected`() = runTest {
        val useCase = IsStoryGrantedUseCase(
            coinPurchaseRepository = FakeCoinPurchaseRepository(),
            userRepository = NotConnectedUserRepository(),
        )

        val result = useCase(listOf("sku-1"))

        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow())
    }

    @Test
    fun `invoke delegates to repository when user is connected`() = runTest {
        val repository = FakeCoinPurchaseRepository()
        repository.setGrantResult(listOf("sku-1"), Result.success(true))
        val useCase = IsStoryGrantedUseCase(
            coinPurchaseRepository = repository,
            userRepository = ConnectedUserRepository(),
        )

        val result = useCase(listOf("sku-1"))

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
    }

    private class ConnectedUserRepository : com.purpletear.sutoko.domain.repository.UserRepository {
        override fun observeUser() = kotlinx.coroutines.flow.flowOf(User("user-1", "token-1"))
        override fun observeIsConnected() = kotlinx.coroutines.flow.flowOf(true)
        override fun isConnected() = Result.success(true)
        override suspend fun connect(id: String, token: String) = Result.success(Unit)
        override suspend fun disconnect() = Result.success(Unit)
    }

    private class NotConnectedUserRepository : com.purpletear.sutoko.domain.repository.UserRepository {
        override fun observeUser() = kotlinx.coroutines.flow.flowOf(null)
        override fun observeIsConnected() = kotlinx.coroutines.flow.flowOf(false)
        override fun isConnected() = Result.success(false)
        override suspend fun connect(id: String, token: String) = Result.success(Unit)
        override suspend fun disconnect() = Result.success(Unit)
    }
}
