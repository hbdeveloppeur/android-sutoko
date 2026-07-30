package com.purpletear.sutoko.shop.data.registrar

import com.purpletear.sutoko.domain.model.User
import com.purpletear.sutoko.shop.test.FakeShopApi
import com.purpletear.sutoko.shop.test.FakeUserRepository
import fr.sutoko.inapppurchase.application.domain.PurchaseRegistrationRejectedException
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class StoryPurchaseBackendRegistrarTest {

    private lateinit var api: FakeShopApi
    private lateinit var userRepository: FakeUserRepository
    private lateinit var registrar: StoryPurchaseBackendRegistrar

    @Before
    fun setUp() {
        api = FakeShopApi()
        userRepository = FakeUserRepository()
        userRepository.userFlow.value = User(id = "user-1", token = "token-1")
        registrar = StoryPurchaseBackendRegistrar(api, userRepository)
    }

    @Test
    fun `supports story sku`() = runTest {
        assertTrue(registrar.supports("story_163"))
    }

    @Test
    fun `does not support incomplete story sku`() = runTest {
        assertFalse(registrar.supports("story_"))
    }

    @Test
    fun `supports premium sku`() = runTest {
        assertTrue(registrar.supports("premium_monthly"))
    }

    @Test
    fun `supports premium sku case-insensitively`() = runTest {
        assertTrue(registrar.supports("Premium_Pass"))
    }

    @Test
    fun `does not support coins pack sku`() = runTest {
        assertFalse(registrar.supports("coins_pack_10"))
    }

    @Test
    fun `register success returns success`() = runTest {
        val result = registrar.register("story_163", "token", "order-1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `register premium sku succeeds via order register`() = runTest {
        val result = registrar.register("premium_month_9_49", "token", "order-1")

        assertTrue(result.isSuccess)
        assertEquals(1, api.registerOrderCallCount)
    }

    @Test
    fun `register 403 returns rejected failure`() = runTest {
        api.setRegisterOrderError(403)

        val result = registrar.register("story_163", "token", "order-1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PurchaseRegistrationRejectedException)
    }

    @Test
    fun `register 400 returns rejected failure`() = runTest {
        api.setRegisterOrderError(400)

        val result = registrar.register("story_163", "token", "order-1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PurchaseRegistrationRejectedException)
    }

    @Test
    fun `register 500 returns retryable failure`() = runTest {
        api.setRegisterOrderError(500)

        val result = registrar.register("story_163", "token", "order-1")

        assertTrue(result.isFailure)
        assertFalse(result.exceptionOrNull() is PurchaseRegistrationRejectedException)
    }

    @Test
    fun `register io exception returns retryable failure`() = runTest {
        api.registerOrderException = IOException("no network")

        val result = registrar.register("story_163", "token", "order-1")

        assertTrue(result.isFailure)
        assertFalse(result.exceptionOrNull() is PurchaseRegistrationRejectedException)
    }

    @Test
    fun `register without user waits for connection before calling the api`() = runTest {
        userRepository.userFlow.value = null

        val pending = backgroundScope.async { registrar.register("story_163", "token", "order-1") }
        runCurrent()

        assertEquals(0, api.registerOrderCallCount)

        userRepository.userFlow.value = User(id = "user-1", token = "token-1")

        assertTrue(pending.await().isSuccess)
        assertEquals(1, api.registerOrderCallCount)
    }
}
