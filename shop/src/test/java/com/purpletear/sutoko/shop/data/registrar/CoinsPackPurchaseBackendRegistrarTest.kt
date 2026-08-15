package com.purpletear.sutoko.shop.data.registrar

import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.shop.data.remote.CoinsBalanceDto
import com.purpletear.sutoko.shop.test.FakeShopApi
import com.purpletear.sutoko.shop.test.FakeShopRepository
import com.purpletear.sutoko.shop.test.FakeUserRepository
import fr.sutoko.inapppurchase.application.domain.PurchaseRegistrationRejectedException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class CoinsPackPurchaseBackendRegistrarTest {

    private lateinit var api: FakeShopApi
    private lateinit var userRepository: FakeUserRepository
    private lateinit var shopRepository: FakeShopRepository
    private lateinit var registrar: CoinsPackPurchaseBackendRegistrar

    @Before
    fun setUp() {
        api = FakeShopApi()
        userRepository = FakeUserRepository()
        userRepository.userFlow.value = User(id = "user-1", token = "token-1")
        shopRepository = FakeShopRepository()
        registrar = CoinsPackPurchaseBackendRegistrar(api, userRepository, shopRepository)
    }

    @Test
    fun `supports every coins pack sku sold by the shop`() = runTest {
        // Invariant pin: these SKUs are the ones hardcoded in InMemoryShopRepository.getPacks().
        assertTrue(registrar.supports("coins_pack_starter"))
        assertTrue(registrar.supports("coins_pack_treasure"))
        assertTrue(registrar.supports("coins_pack_mega"))
    }

    @Test
    fun `does not support story sku`() = runTest {
        assertFalse(registrar.supports("story_163"))
    }

    @Test
    fun `does not support premium sku`() = runTest {
        assertFalse(registrar.supports("premium_monthly"))
    }

    @Test
    fun `register success registers order and updates cached balance`() = runTest {
        api.registerOrderResponse = Response.success(CoinsBalanceDto(coins = 1550, diamonds = 1500))

        val result = registrar.register("coins_pack_treasure", "token", "order-1")

        assertTrue(result.isSuccess)
        assertEquals(1, api.registerOrderCallCount)
        val balance = shopRepository.observeBalance().first()
        assertEquals(1550, balance.coins)
        assertEquals(1500, balance.diamonds)
    }

    @Test
    fun `register without user waits for connection before calling the api`() = runTest {
        userRepository.userFlow.value = null

        val pending = backgroundScope.async {
            registrar.register("coins_pack_starter", "token", "order-1")
        }
        runCurrent()

        assertEquals(0, api.registerOrderCallCount)

        userRepository.userFlow.value = User(id = "user-1", token = "token-1")

        assertTrue(pending.await().isSuccess)
        assertEquals(1, api.registerOrderCallCount)
    }

    @Test
    fun `register 403 returns rejected failure`() = runTest {
        api.setRegisterOrderError(403)

        val result = registrar.register("coins_pack_starter", "token", "order-1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PurchaseRegistrationRejectedException)
    }

    @Test
    fun `register 500 returns retryable failure`() = runTest {
        api.setRegisterOrderError(500)

        val result = registrar.register("coins_pack_starter", "token", "order-1")

        assertTrue(result.isFailure)
        assertFalse(result.exceptionOrNull() is PurchaseRegistrationRejectedException)
    }

    @Test
    fun `register io exception returns retryable failure`() = runTest {
        api.registerOrderException = IOException("no network")

        val result = registrar.register("coins_pack_starter", "token", "order-1")

        assertTrue(result.isFailure)
        assertFalse(result.exceptionOrNull() is PurchaseRegistrationRejectedException)
    }

    @Test
    fun `register success with null body returns retryable failure without touching balance`() = runTest {
        api.registerOrderResponse = Response.success(null)

        val result = registrar.register("coins_pack_treasure", "token", "order-1")

        assertTrue(result.isFailure)
        assertFalse(result.exceptionOrNull() is PurchaseRegistrationRejectedException)
        assertEquals(1, api.registerOrderCallCount)
        val balance = shopRepository.observeBalance().first()
        assertFalse(balance.isLoaded())
    }

    @Test
    fun `register without user fails retryably after the session wait timeout`() = runTest {
        userRepository.userFlow.value = null

        val result = registrar.register("coins_pack_starter", "token", "order-1")

        assertTrue(result.isFailure)
        assertFalse(result.exceptionOrNull() is PurchaseRegistrationRejectedException)
        assertEquals(0, api.registerOrderCallCount)
    }
}
