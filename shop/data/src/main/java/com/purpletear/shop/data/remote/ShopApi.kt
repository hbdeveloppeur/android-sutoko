package com.purpletear.shop.data.remote

import com.purpletear.shop.data.dto.AiCustomerStateDto
import com.purpletear.shop.data.dto.GetAiMessagesPackResponseDto
import com.purpletear.shop.data.dto.TryMessagePackDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.Response

interface ShopApi {

    @FormUrlEncoded
    @POST("user/tokens/count")
    suspend fun getUserMessageCount(
        @Field("userId") userId: String,
        @Field("app_version") appVersion: Int,
        @Field("timeZoneId") timeZoneId: String
    ): Response<AiCustomerStateDto>

    @FormUrlEncoded
    @POST("shop/tokens/buy")
    suspend fun buyTokens(
        @Field("userId") userId: String,
        @Field("token") userToken: String,
        @Field("order_id") orderId: String,
        @Field("purchase_token") purchaseToken: String,
        @Field("product_id") productId: String,
        @Field("app_version") appVersion: Int,
    ): Response<AiCustomerStateDto>

    @FormUrlEncoded
    @POST("shop/tokens/pre-buy")
    suspend fun preBuy(
        @Field("order_id") orderId: String,
        @Field("purchase_token") purchaseToken: String,
        @Field("product_id") productId: String,
        @Field("app_version") appVersion: Int,
    ): Response<AiCustomerStateDto>


    @FormUrlEncoded
    @POST("packs/get-all")
    suspend fun getAiMessagesPacks(
        @Field("langCode") langCode: String
    ): Response<GetAiMessagesPackResponseDto>


    @FormUrlEncoded
    @POST("shop/tokens/try")
    suspend fun tryMessagesPack(
        @Field("userId") userId: String,
        @Field("token") token: String,
        @Field("app_version") appVersion: Int,
    ): Response<TryMessagePackDto>
}