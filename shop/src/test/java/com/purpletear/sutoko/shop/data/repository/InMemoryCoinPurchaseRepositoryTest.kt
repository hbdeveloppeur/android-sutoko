package com.purpletear.sutoko.shop.data.repository

import com.purpletear.sutoko.shop.data.remote.UserHasProductResponseDto
import com.purpletear.sutoko.shop.domain.error.BuyStoryError
import com.purpletear.sutoko.shop.test.FakeShopApi
import com.purpletear.sutoko.shop.test.FakeShopRepository
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
    private lateinit var repository: InMemoryCoinPurchaseRepository

    @Before
    fun setUp() {
        api = FakeShopApi()
        shopRepository = FakeShopRepository()
        repository = InMemoryCoinPurchaseRepository(api, shopRepository)
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
    fun `isStoryGranted returns cached value without calling api`() = runTest {
        repository.addCachedSku("sku-1")

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
}
