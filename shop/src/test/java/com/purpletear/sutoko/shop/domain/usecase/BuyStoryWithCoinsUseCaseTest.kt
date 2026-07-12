package com.purpletear.sutoko.shop.domain.usecase

import com.purpletear.sutoko.domain.exception.NotConnectedException
import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import com.purpletear.sutoko.shop.test.FakeCoinPurchaseRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuyStoryWithCoinsUseCaseTest {

    @Test
    fun `invoke returns failure when user is not connected`() = runTest {
        val useCase = BuyStoryWithCoinsUseCase(
            coinPurchaseRepository = FakeCoinPurchaseRepository(),
            userRepository = NotConnectedUserRepository(),
        )

        val result = useCase("sku-1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NotConnectedException)
    }

    @Test
    fun `invoke delegates to repository when user is connected`() = runTest {
        val repository = FakeCoinPurchaseRepository()
        repository.setBuyResult("sku-1", Result.success(Balance(coins = 50, diamonds = 0)))
        val useCase = BuyStoryWithCoinsUseCase(
            coinPurchaseRepository = repository,
            userRepository = ConnectedUserRepository(),
        )

        val result = useCase("sku-1")

        assertTrue(result.isSuccess)
        assertEquals(50, result.getOrThrow().coins)
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
