package com.purpletear.sutoko.shop.data.repository

import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.shop.data.remote.UserHasProductResponseDto
import com.purpletear.sutoko.shop.domain.error.BuyStoryError
import com.purpletear.sutoko.shop.test.FakeShopApi
import com.purpletear.sutoko.shop.test.FakeShopRepository
import com.purpletear.sutoko.shop.test.FakeUserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class InMemoryCoinPurchaseRepositoryTest {

    private lateinit var api: FakeShopApi
    private lateinit var shopRepository: FakeShopRepository
    private lateinit var userRepository: FakeUserRepository
    private lateinit var repository: InMemoryCoinPurchaseRepository

    @Before
    fun setUp() {
        api = FakeShopApi()
        shopRepository = FakeShopRepository()
        userRepository = FakeUserRepository()
        userRepository.userFlow.value = User(id = "user-1", token = "token-1")
        repository = InMemoryCoinPurchaseRepository(api, shopRepository, userRepository)
    }

    @Test
    fun `buyStoryWithCoins returns balance and caches sku`() = runTest {
        val result = repository.buyStoryWithCoins("sku-1", "user-1")

        assertTrue(result.isSuccess)
        assertEquals(100, result.getOrThrow().coins)
        assertTrue(repository.observeCoinPurchasedSkus().first().contains("sku-1"))
    }

    @Test
    fun `buyStoryWithCoins already owned caches sku and returns failure`() = runTest {
        api.setBuyError(400, "{\"code\":\"ItemAlreadyOwnedError\"}")

        val result = repository.buyStoryWithCoins("sku-1", "user-1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BuyStoryError.AlreadyOwned)
        assertTrue(repository.observeCoinPurchasedSkus().first().contains("sku-1"))
    }

    @Test
    fun `buyStoryWithCoins validation error returns NotPurchasable`() = runTest {
        api.setBuyError(400, "{\"code\":\"ValidationError\"}")

        val result = repository.buyStoryWithCoins("sku-1", "user-1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BuyStoryError.NotPurchasable)
    }

    @Test
    fun `buyStoryWithCoins insufficient funds error returns InsufficientFunds`() = runTest {
        api.setBuyError(400, "{\"code\":\"InsufficientFundsError\"}")

        val result = repository.buyStoryWithCoins("sku-1", "user-1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BuyStoryError.InsufficientFunds)
    }

    @Test
    fun `buyStoryWithCoins http 402 returns InsufficientFunds regardless of body`() = runTest {
        api.setBuyError(402, null)

        val result = repository.buyStoryWithCoins("sku-1", "user-1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BuyStoryError.InsufficientFunds)
    }

    @Test
    fun `isStoryGranted returns cached value without calling api`() = runTest {
        repository.addCachedSku("user-1", "sku-1")

        val result = repository.isStoryGranted("user-1", listOf("sku-1"))

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
        assertEquals(0, api.userHasProductCallCount)
    }

    @Test
    fun `isStoryGranted calls api when not cached`() = runTest {
        api.userHasProductResponse = Response.success(UserHasProductResponseDto(granted = true))

        val result = repository.isStoryGranted("user-1", listOf("sku-1"))

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
        assertEquals(1, api.userHasProductCallCount)
        assertTrue(repository.observeCoinPurchasedSkus().first().contains("sku-1"))
    }

    @Test
    fun `grants cached for another user are not observable`() = runTest {
        repository.addCachedSku("user-a", "sku-1")
        userRepository.userFlow.value = User(id = "user-b", token = "token-b")

        assertEquals(emptySet<String>(), repository.observeCoinPurchasedSkus().first())
    }

    @Test
    fun `grants cached for another user are not returned and api is called`() = runTest {
        repository.addCachedSku("user-a", "sku-1")

        val result = repository.isStoryGranted("user-b", listOf("sku-1"))

        assertTrue(result.isSuccess)
        assertEquals(false, result.getOrThrow())
        assertEquals(1, api.userHasProductCallCount)
    }
}
