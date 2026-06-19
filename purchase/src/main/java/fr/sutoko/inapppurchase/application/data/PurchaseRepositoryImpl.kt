package fr.sutoko.inapppurchase.application.data

import fr.sutoko.inapppurchase.application.data.local.PurchaseDao
import fr.sutoko.inapppurchase.application.data.local.PurchaseEntity
import fr.sutoko.inapppurchase.application.domain.model.Product
import fr.sutoko.inapppurchase.application.domain.model.PurchaseState
import fr.sutoko.inapppurchase.application.domain.repository.PurchaseRepository
import fr.sutoko.inapppurchase.billing.BillingDataSource
import fr.sutoko.inapppurchase.billing.PurchaseReceipt
import fr.sutoko.inapppurchase.billing.PurchaseResult
import fr.sutoko.inapppurchase.billing.exception.PurchaseAlreadyOwnedException
import fr.sutoko.inapppurchase.billing.exception.PurchaseCancelledException
import fr.sutoko.inapppurchase.billing.exception.PurchaseFailedException
import fr.sutoko.inapppurchase.billing.exception.PurchasePendingException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import fr.sutoko.inapppurchase.application.domain.model.Purchase as DomainPurchase

class PurchaseRepositoryImpl @Inject constructor(
    private val purchaseDao: PurchaseDao,
    private val billingDataSource: BillingDataSource,
) : PurchaseRepository {

    override val purchaseUpdates: Flow<Unit> =
        billingDataSource.purchaseUpdates
            .filter { results -> results.any { it is PurchaseResult.Purchased } }
            .map { }

    override val connectionState: Flow<Boolean> =
        billingDataSource.connectionState

    override fun observePurchases(): Flow<List<DomainPurchase>> =
        purchaseDao.observeAll().map { list ->
            list.map { it.toDomain() }
        }

    override fun observePurchase(sku: String): Flow<DomainPurchase?> =
        purchaseDao.observeBySku(sku).map { entity ->
            entity?.toDomain()
        }

    override fun observeHasGlobalPremium(): Flow<Boolean> =
        purchaseDao.observeAll().map { list ->
            list.any {
                it.purchaseState == PurchaseState.PURCHASED &&
                        it.sku.contains("premium", ignoreCase = true)
            }
        }

    override fun observeUnregisteredPurchases(): Flow<List<DomainPurchase>> =
        purchaseDao.observeUnregisteredPurchases(PurchaseState.PURCHASED)
            .map { list -> list.map { it.toDomain() } }

    override fun observePurchasedSkus(): Flow<Set<String>> =
        observePurchases().map { purchases ->
            purchases
                .filter { it.purchaseState == PurchaseState.PURCHASED }
                .map { it.sku }
                .toSet()
        }

    override fun observeIsPurchased(skus: List<String>): Flow<Boolean> =
        combine(
            observePurchasedSkus(),
            observeHasGlobalPremium()
        ) { purchasedSkus, hasGlobalPremium ->
            skus.any { it in purchasedSkus } || hasGlobalPremium
        }

    override suspend fun purchase(sku: String): Result<Unit> {
        if (sku.isBlank()) {
            return Result.failure(IllegalArgumentException("SKU must not be blank"))
        }

        return try {
            when (val purchaseResult = billingDataSource.purchase(sku)) {
                is PurchaseResult.Purchased -> {
                    savePurchase(sku, purchaseResult.receipt)
                    Result.success(Unit)
                }

                is PurchaseResult.Pending -> {
                    savePurchase(sku, purchaseResult.receipt)
                    Result.failure(PurchasePendingException(sku))
                }

                PurchaseResult.Canceled -> {
                    Result.failure(PurchaseCancelledException(sku))
                }

                is PurchaseResult.Failed -> {
                    Result.failure(
                        PurchaseFailedException(
                            sku = sku,
                            responseCode = purchaseResult.responseCode,
                            debugMessage = purchaseResult.message
                        )
                    )
                }

                is PurchaseResult.AlreadyOwned -> {
                    Result.failure(
                        PurchaseAlreadyOwnedException(
                            sku = sku,
                        )
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun savePurchase(
        sku: String,
        receipt: PurchaseReceipt,
    ) {
        purchaseDao.upsert(
            PurchaseEntity(
                sku = sku,
                purchaseToken = receipt.purchaseToken,
                purchaseTime = receipt.purchaseTime,
                acknowledged = receipt.acknowledged,
                purchaseState = receipt.purchaseState,
                orderId = receipt.orderId,
                backendRegistered = false,
            )
        )
    }

    override suspend fun queryProductDetails(sku: String): Result<Product> =
        queryProductDetails(listOf(sku)).map { it.first() }

    override suspend fun queryProductDetails(skus: List<String>): Result<List<Product>> {
        if (skus.isEmpty()) {
            return Result.success(emptyList())
        }

        if (skus.any { it.isBlank() }) {
            return Result.failure(IllegalArgumentException("SKU must not be blank"))
        }

        return try {
            val details = billingDataSource.queryProductDetails(skus)
            val foundSkus = details.map { it.sku }.toSet()
            val missingSku = skus.firstOrNull { it !in foundSkus }

            if (missingSku != null) {
                Result.failure(
                    IllegalArgumentException("Product details not found for sku: $missingSku")
                )
            } else {
                Result.success(
                    details.map {
                        Product(
                            sku = it.sku,
                            title = it.title,
                            description = it.description,
                            formattedPrice = it.formattedPrice
                        )
                    }
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncPurchases(): Result<Unit> {
        return try {
            billingDataSource.reconcilePurchases()

            val receipts = billingDataSource.queryPurchases()

            val existing = purchaseDao.observeAll().first().associateBy { it.sku }

            val entities = receipts.map { receipt ->
                val previous = existing[receipt.sku]
                val samePurchaseToken = previous?.purchaseToken == receipt.purchaseToken
                PurchaseEntity(
                    sku = receipt.sku,
                    purchaseToken = receipt.purchaseToken,
                    purchaseTime = receipt.purchaseTime,
                    acknowledged = receipt.acknowledged,
                    purchaseState = receipt.purchaseState,
                    orderId = receipt.orderId,
                    backendRegistered = previous?.backendRegistered == true && samePurchaseToken,
                )
            }

            purchaseDao.replaceAll(entities)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override suspend fun markBackendRegistered(sku: String) {
        purchaseDao.markBackendRegistered(sku)
    }
}

private fun PurchaseEntity.toDomain(): DomainPurchase =
    DomainPurchase(
        sku = sku,
        purchaseToken = purchaseToken,
        purchaseTime = purchaseTime,
        acknowledged = acknowledged,
        purchaseState = purchaseState,
        orderId = orderId,
        backendRegistered = backendRegistered,
    )
