package com.purpletear.shop.domain.repository

import com.purpletear.shop.domain.model.Balance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for catalog operations.
 */
interface CatalogRepository {
    val shopBalance: StateFlow<Balance?>

    /**
     * Buy a catalog product
     *
     * @param userId The ID of the user making the purchase
     * @param skuIdentifier The identifier of the product to buy
     * @param type The type of the product
     * @return A Flow containing the Result of the purchase operation
     */
    suspend fun buyCatalogProduct(
        userId: String,
        skuIdentifier: String,
        type: String
    ): Flow<Result<Unit>>

    /**
     * Check if a user has specific products
     *
     * @param userId The ID of the user to check
     * @param skuIdentifiers List of product identifiers to check
     * @return A Flow containing the Result of the check operation with a boolean indicating if the user has the products
     */
    suspend fun userHasProduct(
        userId: String,
        skuIdentifiers: List<String>
    ): Flow<Result<Boolean>>

    /**
     * Register an order with the catalog service
     *
     * @param purchaseToken The purchase token from the payment provider
     * @param skuIdentifier SKU identifier for the product in the order
     * @param userId The ID of the user making the purchase
     * @param userToken The authentication token of the user
     * @return A Flow containing the Result of the registration operation
     */
    suspend fun registerOrder(
        purchaseToken: String,
        skuIdentifier: String,
        userId: String,
        userToken: String
    ): Flow<Result<Unit>>

    /**
     * Adds the specified product identifiers to the list of acknowledged orders.
     *
     * @param purchaseTokens */
    fun addToAcknowledgedOrders(purchaseTokens: Array<String>)

    /**
     * Checks whether the specified orders, identified by their purchase tokens, are acknowledged.
     *
     * @param purchaseTokens An array of purchase tokens for the orders to be checked.
     * @return A map where each key is a purchase token, and the value is a boolean indicating whether the corresponding order is acknowledged.
     */
    fun isAcknowledgedOrders(purchaseTokens: Array<String>): Map<String, Boolean>

    /**
     * Get the user's balance of coins and diamonds
     *
     * @param userId The ID of the user
     * @param userToken The authentication token of the user
     * @return A Flow containing the Result of the balance retrieval operation
     */
    suspend fun getBalance(
        userId: String,
        userToken: String
    ): Flow<Result<Balance>>
}
