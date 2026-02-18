package com.purpletear.shop.data.dto

import androidx.annotation.Keep
import com.purpletear.shop.domain.model.Balance

/**
 * Data class representing the response from the getBalance API call.
 *
 * @property coinsBalance The balance information containing coins and diamonds
 */
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