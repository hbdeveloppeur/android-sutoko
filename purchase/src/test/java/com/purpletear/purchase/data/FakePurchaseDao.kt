package com.purpletear.purchase.data

import fr.sutoko.inapppurchase.application.data.local.PurchaseDao
import fr.sutoko.inapppurchase.application.data.local.PurchaseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicInteger

class FakePurchaseDao : PurchaseDao {

    private val _purchases = MutableStateFlow<List<PurchaseEntity>>(emptyList())

    val purchases: List<PurchaseEntity> get() = _purchases.value
    val upsertedEntities = mutableListOf<PurchaseEntity>()
    val reconcileCalls = mutableListOf<List<PurchaseEntity>>()
    val markedBackendRegisteredSkus = mutableListOf<String>()
    val deletedSkus = mutableListOf<String>()

    val observeAllSubscriptionCount = AtomicInteger(0)

    override fun observeAll(): Flow<List<PurchaseEntity>> =
        _purchases
            .onStart { observeAllSubscriptionCount.incrementAndGet() }
            .onCompletion { observeAllSubscriptionCount.decrementAndGet() }

    override fun observeBySku(sku: String): Flow<PurchaseEntity?> =
        _purchases.map { list -> list.find { it.sku == sku } }

    override fun observeUnregisteredPurchases(purchasedState: Int): Flow<List<PurchaseEntity>> =
        _purchases.map { list ->
            list.filter { it.purchaseState == purchasedState && !it.backendRegistered }
        }

    override suspend fun markBackendRegistered(sku: String) {
        markedBackendRegisteredSkus += sku
        _purchases.update { list ->
            list.map { if (it.sku == sku) it.copy(backendRegistered = true) else it }
        }
    }

    override suspend fun upsert(entity: PurchaseEntity) {
        upsertedEntities += entity
        _purchases.update { list -> list.filter { it.sku != entity.sku } + entity }
    }

    override suspend fun upsertAll(entities: List<PurchaseEntity>) {
        val skus = entities.map { it.sku }.toSet()
        _purchases.update { list -> list.filter { it.sku !in skus } + entities }
    }

    override fun observePurchaseCountForSkus(skus: List<String>): Flow<Int> =
        _purchases.map { list -> list.count { it.sku in skus } }

    override suspend fun deleteBySku(sku: String) {
        deletedSkus += sku
        _purchases.update { list -> list.filter { it.sku != sku } }
    }

    override suspend fun deleteRegisteredMissingFrom(skus: List<String>) {
        _purchases.update { list ->
            list.filter { !it.backendRegistered || it.sku in skus }
        }
    }

    override suspend fun reconcile(entities: List<PurchaseEntity>) {
        reconcileCalls += entities.toList()
        upsertAll(entities)
        deleteRegisteredMissingFrom(entities.map { it.sku })
    }
}
