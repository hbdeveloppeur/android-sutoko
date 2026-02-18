package com.purpletear.shop.data.remote

import com.purpletear.shop.data.dto.BuyCatalogProductRequestDto
import com.purpletear.shop.data.dto.CoinsBalanceDto
import com.purpletear.shop.data.dto.GetBalanceRequestDto
import com.purpletear.shop.data.dto.GetBalanceResponseDto
import com.purpletear.shop.data.dto.RegisterOrderRequestDto
import com.purpletear.shop.data.dto.UserHasProductRequestDto
import com.purpletear.shop.data.dto.UserHasProductResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API interface for accessing Catalog data from the remote server.
 */
interface CatalogApi {
    /**
     * Buy a catalog product
     *
     * @param request The request body containing product identifier, user ID, and product type
     * @return A Response containing the result of the purchase operation
     */
    @POST("shop/buy")
    suspend fun buyCatalogProduct(
        @Body request: BuyCatalogProductRequestDto
    ): Response<Unit>

    /**
     * Check if a user has specific products
     *
     * @param request The request body containing user ID and list of product identifiers to check
     * @return A Response containing the result of the check operation
     */
    @POST("product/is-granted")
    suspend fun userHasProduct(
        @Body request: UserHasProductRequestDto
    ): Response<UserHasProductResponseDto>

    /**
     * Register an order with the catalog service
     *
     * @param request The request body containing purchase token, SKU identifiers, user ID, and user token
     * @return A Response indicating the success of the operation
     */
    @POST("order/register")
    suspend fun registerOrder(
        @Body request: RegisterOrderRequestDto
    ): Response<CoinsBalanceDto>

    /**
     * Get the user's balance of coins and diamonds
     *
     * @param request The request body containing user ID and user token
     * @return A Response containing the balance information
     */
    @POST("coins/balance")
    suspend fun getBalance(
        @Body request: GetBalanceRequestDto
    ): Response<GetBalanceResponseDto>
}
