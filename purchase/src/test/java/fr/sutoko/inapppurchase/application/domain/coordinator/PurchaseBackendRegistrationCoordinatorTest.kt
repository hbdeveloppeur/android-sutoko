package fr.sutoko.inapppurchase.application.domain.coordinator

import fr.sutoko.inapppurchase.application.domain.PurchaseBackendRegistrar
import fr.sutoko.inapppurchase.application.domain.PurchaseRegistrationRejectedException
import fr.sutoko.inapppurchase.application.domain.model.Product
import fr.sutoko.inapppurchase.application.domain.model.Purchase
import fr.sutoko.inapppurchase.application.domain.model.PurchaseState
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PurchaseBackendRegistrationCoordinatorTest {

    private val fakeRepository = FakePurchaseRepository()

    @Test
    fun `registrar success marks purchase as backend registered`() = runTest {
        val registrar = FakeRegistrar(supportedSkus = setOf("sku"))
        fakeRepository.addPurchase(purchase(sku = "sku"))

        coordinator(registrar).start(backgroundScope)
        runCurrent()
        advanceUntilIdle()

        assertEquals(listOf("sku"), registrar.registerCalls)
        assertEquals(listOf("sku"), fakeRepository.markedBackendRegisteredSkus)
        assertTrue(fakeRepository.deletedSkus.isEmpty())
    }

    @Test
    fun `unclaimed SKU is marked as backend registered without any registration`() = runTest {
        val registrar = FakeRegistrar(supportedSkus = setOf("other-sku"))
        fakeRepository.addPurchase(purchase(sku = "sku"))

        coordinator(registrar).start(backgroundScope)
        runCurrent()
        advanceUntilIdle()

        assertTrue(registrar.registerCalls.isEmpty())
        assertEquals(listOf("sku"), fakeRepository.markedBackendRegisteredSkus)
        assertTrue(fakeRepository.deletedSkus.isEmpty())
    }

    @Test
    fun `backend rejection purges local purchase without retrying`() = runTest {
        val registrar = FakeRegistrar(
            supportedSkus = setOf("sku"),
            results = ArrayDeque(
                listOf(
                    Result.failure<Unit>(
                        PurchaseRegistrationRejectedException("invalid purchase token")
                    )
                )
            )
        )
        fakeRepository.addPurchase(purchase(sku = "sku"))

        coordinator(registrar).start(backgroundScope)
        runCurrent()
        advanceUntilIdle()

        assertEquals(listOf("sku"), registrar.registerCalls)
        assertEquals(listOf("sku"), fakeRepository.deletedSkus)
        assertTrue(fakeRepository.markedBackendRegisteredSkus.isEmpty())
        assertTrue(fakeRepository.purchases.value.isEmpty())
    }

    @Test
    fun `transient failure is retried and eventual success marks backend registered`() = runTest {
        val registrar = FakeRegistrar(
            supportedSkus = setOf("sku"),
            results = ArrayDeque(
                listOf(
                    Result.failure<Unit>(IOException("network error")),
                    Result.success(Unit)
                )
            )
        )
        fakeRepository.addPurchase(purchase(sku = "sku"))

        coordinator(registrar).start(backgroundScope)
        runCurrent()
        // First attempt fails, backoff delay must elapse before the retry succeeds.
        advanceTimeBy(5_000)
        runCurrent()

        assertTrue(registrar.registerCalls.size > 1)
        assertEquals(listOf("sku"), fakeRepository.markedBackendRegisteredSkus)
        assertTrue(fakeRepository.deletedSkus.isEmpty())
    }

    private fun coordinator(vararg registrars: PurchaseBackendRegistrar) =
        PurchaseBackendRegistrationCoordinator(fakeRepository, registrars.toSet())

    private fun purchase(
        sku: String = "sku",
        purchaseToken: String = "token",
        purchaseTime: Long = 1_000,
        acknowledged: Boolean = true,
        purchaseState: Int = PurchaseState.PURCHASED,
        orderId: String? = "order-1",
        backendRegistered: Boolean = false,
    ): Purchase = Purchase(
        sku = sku,
        purchaseToken = purchaseToken,
        purchaseTime = purchaseTime,
        acknowledged = acknowledged,
        purchaseState = purchaseState,
        orderId = orderId,
        backendRegistered = backendRegistered,
    )

    private class FakeRegistrar(
        private val supportedSkus: Set<String>,
        private val results: ArrayDeque<Result<Unit>> = ArrayDeque(),
    ) : PurchaseBackendRegistrar {

        val registerCalls = mutableListOf<String>()

        override suspend fun supports(sku: String): Boolean = sku in supportedSkus

        override suspend fun register(
            sku: String,
            purchaseToken: String,
            orderId: String?,
        ): Result<Unit> {
            registerCalls += sku
            return results.removeFirstOrNull() ?: Result.success(Unit)
        }
    }

    private class FakePurchaseRepository : PurchaseRepository {

        val purchases = MutableStateFlow<List<Purchase>>(emptyList())
        val markedBackendRegisteredSkus = mutableListOf<String>()
        val deletedSkus = mutableListOf<String>()

        override val purchaseUpdates: Flow<Unit> = emptyFlow()
        override val purchaseProcessing: Flow<String> = emptyFlow()
        override val connectionState: Flow<Boolean> = MutableStateFlow(true)

        fun addPurchase(purchase: Purchase) {
            purchases.update { list ->
                list.filter { it.sku != purchase.sku } + purchase
            }
        }

        override fun observePurchases(): Flow<List<Purchase>> = purchases

        override fun observePurchase(sku: String): Flow<Purchase?> =
            purchases.map { list -> list.find { it.sku == sku } }

        override fun observeHasGlobalPremium(): Flow<Boolean> =
            purchases.map { list ->
                list.any {
                    it.purchaseState == PurchaseState.PURCHASED &&
                            it.sku.contains("premium", ignoreCase = true)
                }
            }

        override fun observeUnregisteredPurchases(): Flow<List<Purchase>> =
            purchases.map { list ->
                list.filter { it.purchaseState == PurchaseState.PURCHASED && !it.backendRegistered }
            }

        override fun observePurchasedSkus(): Flow<Set<String>> =
            purchases.map { list ->
                list.filter { it.purchaseState == PurchaseState.PURCHASED }
                    .map { it.sku }
                    .toSet()
            }

        override fun observeIsPurchased(skus: List<String>): Flow<Boolean> =
            observePurchasedSkus().map { purchasedSkus -> skus.any { it in purchasedSkus } }

        override suspend fun purchase(sku: String): Result<Unit> = Result.success(Unit)

        override suspend fun syncPurchases(): Result<Unit> = Result.success(Unit)

        override suspend fun queryProductDetails(sku: String): Result<Product> =
            Result.failure(UnsupportedOperationException())

        override suspend fun queryProductDetails(skus: List<String>): Result<List<Product>> =
            Result.failure(UnsupportedOperationException())

        override suspend fun markBackendRegistered(sku: String) {
            markedBackendRegisteredSkus += sku
            purchases.update { list ->
                list.map { if (it.sku == sku) it.copy(backendRegistered = true) else it }
            }
        }

        override suspend fun deletePurchase(sku: String) {
            deletedSkus += sku
            purchases.update { list -> list.filter { it.sku != sku } }
        }
    }
}
