package com.purpletear.sutoko.shop.data.remote

import androidx.annotation.Keep
import com.purpletear.sutoko.shop.domain.repository.model.Balance
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ShopApi {
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

@Keep
data class BuyCatalogProductRequestDto(
    val skuIdentifier: String,
    val userId: String,
    val type: String
)

@Keep
data class UserHasProductRequestDto(
    val userId: String,
    val skuIdentifiers: List<String>
)

@Keep
data class UserHasProductResponseDto(
    val granted: Boolean
)

@Keep
data class RegisterOrderRequestDto(
    val purchaseToken: String,
    val skuIdentifier: String,
    val userId: String,
    val userToken: String
)

@Keep
data class GetBalanceRequestDto(
    val userId: String,
    val userToken: String
)

@Keep
data class GetBalanceResponseDto(
    val coinsBalance: CoinsBalanceDto
) {
    /**
     * Converts the DTO to a domain model.
     *
     * @return A Balance domain model
     */
    fun toDomainModel(): Balance {
        return coinsBalance.toDomainModel()
    }
}

/**
 * Data class representing the coins balance information.
 *
 * @property coins The number of coins
 * @property diamonds The number of diamonds
 */
@Keep
data class CoinsBalanceDto(
    val coins: Int,
    val diamonds: Int
)

fun CoinsBalanceDto.toDomainModel(): Balance = Balance(
    coins = coins,
    diamonds = diamonds
)