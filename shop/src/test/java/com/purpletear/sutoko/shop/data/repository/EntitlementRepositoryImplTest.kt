package com.purpletear.sutoko.shop.data.repository

import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.shop.test.FakeCoinPurchaseRepository
import com.purpletear.sutoko.shop.test.FakePurchaseRepository
import com.purpletear.sutoko.shop.test.FakeUserRepository
import fr.sutoko.inapppurchase.application.domain.model.Purchase
import fr.sutoko.inapppurchase.application.domain.model.PurchaseState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EntitlementRepositoryImplTest {

    private lateinit var purchaseRepository: FakePurchaseRepository
    private lateinit var coinPurchaseRepository: FakeCoinPurchaseRepository
    private lateinit var userRepository: FakeUserRepository
    private lateinit var repository: EntitlementRepositoryImpl

    @Before
    fun setUp() {
        purchaseRepository = FakePurchaseRepository()
        coinPurchaseRepository = FakeCoinPurchaseRepository()
        userRepository = FakeUserRepository()
        repository = EntitlementRepositoryImpl(
            purchaseRepository,
            coinPurchaseRepository,
            userRepository
        )
    }

    @Test
    fun `registered billing purchase grants sku`() = runTest {
        purchaseRepository.purchasesFlow.value = listOf(
            purchase(sku = "story_163", backendRegistered = true)
        )

        assertTrue(repository.observeIsGranted(listOf("story_163")).first())
    }

    @Test
    fun `unregistered billing purchase does not grant sku`() = runTest {
        purchaseRepository.purchasesFlow.value = listOf(
            purchase(sku = "story_163", backendRegistered = false)
        )

        assertFalse(repository.observeIsGranted(listOf("story_163")).first())
    }

    @Test
    fun `coin purchased sku grants sku`() = runTest {
        coinPurchaseRepository.setCachedSkus(setOf("story_163"))

        assertTrue(repository.observeIsGranted(listOf("story_163")).first())
    }

    @Test
    fun `registered premium purchase grants any sku and premium`() = runTest {
        purchaseRepository.purchasesFlow.value = listOf(
            purchase(sku = "premium_monthly", backendRegistered = true)
        )

        assertTrue(repository.observeIsGranted(listOf("story_163")).first())
        assertTrue(repository.observeHasPremium().first())
    }

    @Test
    fun `nothing confirmed emits false`() = runTest {
        assertFalse(repository.observeIsGranted(listOf("story_163")).first())
        assertFalse(repository.observeHasPremium().first())
    }

    @Test
    fun `granted skus is union of registered billing skus and coin skus`() = runTest {
        purchaseRepository.purchasesFlow.value = listOf(
            purchase(sku = "story_163", backendRegistered = true),
            purchase(sku = "story_164", backendRegistered = false)
        )
        coinPurchaseRepository.setCachedSkus(setOf("story_165"))

        assertEquals(
            setOf("story_163", "story_165"),
            repository.observeGrantedSkus().first()
        )
    }

    @Test
    fun `refreshGrant without user returns failure`() = runTest {
        val result = repository.refreshGrant(listOf("story_163"))

        assertTrue(result.isFailure)
    }

    @Test
    fun `refreshGrant delegates to coin purchase repository`() = runTest {
        userRepository.userFlow.value = User(id = "user-1", token = "token-1")
        coinPurchaseRepository.setGrantResult(listOf("story_163"), Result.success(true))

        val result = repository.refreshGrant(listOf("story_163"))

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
    }

    private fun purchase(sku: String, backendRegistered: Boolean) = Purchase(
        sku = sku,
        purchaseToken = "token",
        purchaseTime = 0L,
        acknowledged = true,
        purchaseState = PurchaseState.PURCHASED,
        orderId = "order-1",
        backendRegistered = backendRegistered,
    )
}
