package com.purpletear.sutoko.shop.domain.usecase

import com.purpletear.sutoko.shop.domain.model.PackItem
import com.purpletear.sutoko.shop.domain.repository.ShopRepository
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import com.purpletear.sutoko.shop.domain.repository.model.CoinsPackType
import com.purpletear.sutoko.shop.domain.repository.model.ShopPack
import com.purpletear.sutoko.shop.test.FakePurchaseRepository
import fr.sutoko.inapppurchase.application.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetShopPackPricesUseCaseTest {

    private val fakeShopRepository = FakeShopRepository()
    private val getShopPacksUseCase = GetShopPacksUseCase(fakeShopRepository)
    private val fakePurchaseRepository = FakePurchaseRepository()
    private val useCase = GetShopPackPricesUseCase(
        getShopPacksUseCase = getShopPacksUseCase,
        purchaseRepository = fakePurchaseRepository,
    )

    @Test
    fun `returns packs with formatted prices when billing details are available`() = runTest {
        fakeShopRepository.packs = listOf(
            ShopPack(coins = 100, diamonds = 100, sku = "low", type = CoinsPackType.Low),
            ShopPack(coins = 500, diamonds = 500, sku = "high", type = CoinsPackType.High),
        )
        fakePurchaseRepository.queryProductDetailsResult = mapOf(
            "low" to Result.success(product("low", "$0.99")),
            "high" to Result.success(product("high", "$4.99")),
        )

        val result = useCase()

        assertTrue(result.isSuccess)
        val packs = result.getOrThrow()
        assertEquals(2, packs.size)
        assertPackPrice(packs, CoinsPackType.Low, "$0.99")
        assertPackPrice(packs, CoinsPackType.High, "$4.99")
    }

    @Test
    fun `returns packs with null price for missing billing details`() = runTest {
        fakeShopRepository.packs = listOf(
            ShopPack(coins = 100, diamonds = 100, sku = "low", type = CoinsPackType.Low),
            ShopPack(coins = 500, diamonds = 500, sku = "missing", type = CoinsPackType.High),
        )
        fakePurchaseRepository.queryProductDetailsResult = mapOf(
            "low" to Result.success(product("low", "$0.99")),
        )

        val result = useCase()

        assertTrue(result.isSuccess)
        val packs = result.getOrThrow()
        assertEquals(2, packs.size)
        assertPackPrice(packs, CoinsPackType.Low, "$0.99")
        assertNullPrice(packs, CoinsPackType.High)
    }

    @Test
    fun `returns failure when pack list cannot be loaded`() = runTest {
        fakeShopRepository.packsResult = Result.failure(RuntimeException("network error"))

        val result = useCase()

        assertTrue(result.isFailure)
    }

    private fun assertPackPrice(
        packs: List<PackItem>,
        type: CoinsPackType,
        expected: String
    ) {
        val item = packs.first { it.pack.type == type }
        assertEquals(expected, item.formattedPrice)
    }

    private fun assertNullPrice(packs: List<PackItem>, type: CoinsPackType) {
        val item = packs.first { it.pack.type == type }
        assertNull(item.formattedPrice)
    }

    private fun product(sku: String, price: String) = Product(
        sku = sku,
        title = sku,
        description = sku,
        formattedPrice = price,
    )

    private class FakeShopRepository : ShopRepository {
        var packs: List<ShopPack> = emptyList()
        var packsResult: Result<List<ShopPack>>? = null

        override fun observeBalance(): Flow<Balance> = flowOf()
        override fun loadBalance(userId: String, userToken: String): Flow<Result<Unit>> = flowOf()

        override fun resetBalance() { /* no-op: balance is not exercised in these tests */ }
        override fun updateBalance(balance: Balance) { /* no-op */ }

        override suspend fun getPacks(): Result<List<ShopPack>> {
            return packsResult ?: Result.success(packs)
        }
    }
}
