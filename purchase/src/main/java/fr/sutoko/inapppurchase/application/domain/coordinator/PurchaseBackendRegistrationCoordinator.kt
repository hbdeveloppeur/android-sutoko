package fr.sutoko.inapppurchase.application.domain.coordinator

import android.util.Log
import fr.sutoko.inapppurchase.application.domain.PurchaseBackendRegistrar
import fr.sutoko.inapppurchase.application.domain.PurchaseRegistrationRejectedException
import fr.sutoko.inapppurchase.application.domain.model.PurchaseState
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import fr.sutoko.inapppurchase.application.domain.model.Purchase as DomainPurchase

/**
 * Drains the local queue of purchases that need backend registration.
 *
 * Watches [PurchaseRepository.observeUnregisteredPurchases] and calls each
 * [PurchaseBackendRegistrar] for every PURCHASED item. Successful registrations
 * are marked in the local DB so the same purchase is never registered twice.
 * Failures are retried with exponential backoff up to a bounded limit.
 */
@Singleton
class PurchaseBackendRegistrationCoordinator @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val registrars: Set<@JvmSuppressWildcards PurchaseBackendRegistrar>,
) {

    fun start(scope: CoroutineScope) {
        scope.launch {
            purchaseRepository.observeUnregisteredPurchases().collect { purchases ->
                purchases.forEach { purchase ->
                    registerWithRetry(purchase)
                }
            }
        }
    }

    private suspend fun registerWithRetry(purchase: DomainPurchase) {
        val supportedRegistrars = registrars.filter { it.supports(purchase.sku) }

        if (supportedRegistrars.isEmpty()) {
            // Contract breach: every purchasable SKU must have a registrar. An
            // unclaimed SKU means the backend is never told about the purchase,
            // so the user pays without being credited. This must be loud.
            Log.e(
                "BackendRegistration",
                "No registrar claims ${purchase.sku}; marking as registered without backend call"
            )
            purchaseRepository.markBackendRegistered(purchase.sku)
            return
        }

        var attempt = 0

        while (attempt < MAX_RETRIES) {
            if (purchase.purchaseState != PurchaseState.PURCHASED) {
                return
            }

            val results = supportedRegistrars.map { registrar ->
                registrar.register(purchase.sku, purchase.purchaseToken, purchase.orderId)
            }

            val rejection = results.firstNotNullOfOrNull { result ->
                result.exceptionOrNull() as? PurchaseRegistrationRejectedException
            }
            if (rejection != null) {
                Log.w(
                    "BackendRegistration",
                    "Backend rejected registration for ${purchase.sku}; purging local purchase",
                    rejection
                )
                purchaseRepository.deletePurchase(purchase.sku)
                return
            }

            if (results.all { it.isSuccess }) {
                purchaseRepository.markBackendRegistered(purchase.sku)
                return
            }

            attempt++
            val failure = results.firstOrNull { it.isFailure }?.exceptionOrNull()
            Log.w(
                "BackendRegistration",
                "Backend registration failed for ${purchase.sku} (attempt $attempt/$MAX_RETRIES)",
                failure
            )

            if (attempt < MAX_RETRIES) {
                delay(backoffDelay(attempt))
            }
        }

        Log.e(
            "BackendRegistration",
            "Giving up backend registration for ${purchase.sku} after $MAX_RETRIES attempts"
        )
    }

    private fun backoffDelay(attempt: Int): Duration {
        return minOf(BASE_DELAY * (1 shl attempt), MAX_DELAY)
    }

    private companion object {
        val BASE_DELAY = 1.seconds
        val MAX_DELAY = 30.seconds
        const val MAX_RETRIES = 5
    }
}
