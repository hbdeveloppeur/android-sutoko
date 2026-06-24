package fr.purpletear.sutoko.sync.purchase

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import fr.purpletear.sutoko.BuildConfig
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates purchase reconciliation at the application lifecycle boundary.
 *
 * Triggers sync:
 * - when the app returns to foreground,
 * - when the billing layer reports a new purchase via [PurchaseRepository.purchaseUpdates],
 * - when the billing connection is re-established after a disconnect.
 */
@Singleton
class PurchaseSyncCoordinator @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
) {

    private val syncMutex = Mutex()
    private var startupLogDone = false

    fun start(lifecycle: Lifecycle, scope: CoroutineScope) {
        lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    scope.launch { sync() }
                }
            }
        )

        scope.launch {
            purchaseRepository.purchaseUpdates.collect { sync() }
        }

        scope.launch {
            observeReconnectAndSync()
        }
    }

    private suspend fun observeReconnectAndSync() {
        var wasEverConnected = false
        purchaseRepository.connectionState
            .filter { it }
            .collect {
                if (wasEverConnected) {
                    sync()
                }
                wasEverConnected = true
            }
    }

    private suspend fun sync() = syncMutex.withLock {
        purchaseRepository.syncPurchases()
            .onFailure { Log.w("PurchaseSyncCoordinator", "Purchase sync failed", it) }
            .onSuccess {
                if (BuildConfig.DEBUG && !startupLogDone) {
                    startupLogDone = true
                    runCatching {
                        val skus = purchaseRepository.observePurchasedSkus().first()
                        Log.d("BoughtSkus", "User bought SKUs: $skus")
                    }
                }
            }
    }
}
