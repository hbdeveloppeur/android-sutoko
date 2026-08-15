package com.purpletear.sutoko.shop.data.repository

import com.purpletear.sutoko.shop.data.remote.CoinsBalanceDto
import com.purpletear.sutoko.shop.data.remote.GetBalanceResponseDto
import com.purpletear.sutoko.shop.test.FakeShopApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class InMemoryShopRepositoryTest {

    private val api = FakeShopApi()
    private val repository = InMemoryShopRepository(api)

    @Test
    fun `failed reload keeps previously loaded balance and flags loadFailed`() = runTest {
        api.getBalanceResponse =
            Response.success(GetBalanceResponseDto(CoinsBalanceDto(coins = 1500, diamonds = 1500)))
        repository.loadBalance("user-1", "token-1").first()

        api.setGetBalanceError(500)
        val result = repository.loadBalance("user-1", "token-1").first()

        assertTrue(result.isFailure)
        val balance = repository.observeBalance().first()
        assertEquals(1500, balance.coins)
        assertEquals(1500, balance.diamonds)
        assertTrue(balance.isLoaded())
        assertTrue(balance.loadFailed)
    }

    @Test
    fun `failed load without previous balance keeps the unloaded sentinel`() = runTest {
        api.setGetBalanceError(500)

        val result = repository.loadBalance("user-1", "token-1").first()

        assertTrue(result.isFailure)
        val balance = repository.observeBalance().first()
        assertFalse(balance.isLoaded())
        assertTrue(balance.loadFailed)
    }

    @Test
    fun `successful reload after failure clears loadFailed`() = runTest {
        api.setGetBalanceError(500)
        repository.loadBalance("user-1", "token-1").first()

        api.getBalanceResponse =
            Response.success(GetBalanceResponseDto(CoinsBalanceDto(coins = 42, diamonds = 7)))
        val result = repository.loadBalance("user-1", "token-1").first()

        assertTrue(result.isSuccess)
        val balance = repository.observeBalance().first()
        assertEquals(42, balance.coins)
        assertFalse(balance.loadFailed)
    }
}
