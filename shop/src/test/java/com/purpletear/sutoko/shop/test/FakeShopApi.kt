package com.purpletear.sutoko.shop.test

import com.purpletear.sutoko.shop.data.remote.BuyCatalogProductRequestDto
import com.purpletear.sutoko.shop.data.remote.BuyCatalogProductResponseDto
import com.purpletear.sutoko.shop.data.remote.CoinsBalanceDto
import com.purpletear.sutoko.shop.data.remote.GetBalanceRequestDto
import com.purpletear.sutoko.shop.data.remote.GetBalanceResponseDto
import com.purpletear.sutoko.shop.data.remote.RegisterOrderRequestDto
import com.purpletear.sutoko.shop.data.remote.ShopApi
import com.purpletear.sutoko.shop.data.remote.UserHasProductRequestDto
import com.purpletear.sutoko.shop.data.remote.UserHasProductResponseDto
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

class FakeShopApi : ShopApi {

    var buyCatalogProductResponse: Response<BuyCatalogProductResponseDto> =
        Response.success(BuyCatalogProductResponseDto(CoinsBalanceDto(coins = 100, diamonds = 0)))

    var userHasProductResponse: Response<UserHasProductResponseDto> =
        Response.success(UserHasProductResponseDto(granted = false))

    var buyCatalogProductCallCount = 0
        private set

    var userHasProductCallCount = 0
        private set

    override suspend fun buyCatalogProduct(
        request: BuyCatalogProductRequestDto
    ): Response<BuyCatalogProductResponseDto> {
        buyCatalogProductCallCount++
        return buyCatalogProductResponse
    }

    override suspend fun userHasProduct(
        request: UserHasProductRequestDto
    ): Response<UserHasProductResponseDto> {
        userHasProductCallCount++
        return userHasProductResponse
    }

    override suspend fun registerOrder(
        request: RegisterOrderRequestDto
    ): Response<CoinsBalanceDto> = Response.success(CoinsBalanceDto(coins = 0, diamonds = 0))

    override suspend fun getBalance(
        request: GetBalanceRequestDto
    ): Response<GetBalanceResponseDto> = Response.success(
        GetBalanceResponseDto(CoinsBalanceDto(coins = 0, diamonds = 0))
    )

    fun setBuyError(code: Int, body: String) {
        buyCatalogProductResponse = Response.error(
            code,
            body.toResponseBody()
        )
    }
}
