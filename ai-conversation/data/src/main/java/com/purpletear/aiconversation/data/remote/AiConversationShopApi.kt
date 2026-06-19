package com.purpletear.aiconversation.data.remote

import com.purpletear.aiconversation.data.remote.dto.AiTokensStateDto
import com.purpletear.aiconversation.data.remote.dto.GetAiMessagesPackResponseDto
import com.purpletear.aiconversation.data.remote.dto.TryMessagePackDto
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


interface ShopApi {

    @FormUrlEncoded
    @POST("user/tokens/count")
    suspend fun getUserMessageCount(
        @Field("userId") userId: String,
        @Field("app_version") appVersion: Int,
        @Field("timeZoneId") timeZoneId: String
    ): Response<AiTokensStateDto>

    @FormUrlEncoded
    @POST("shop/tokens/buy")
    suspend fun buyTokens(
        @Field("userId") userId: String,
        @Field("token") userToken: String,
        @Field("order_id") orderId: String,
        @Field("purchase_token") purchaseToken: String,
        @Field("product_id") productId: String,
        @Field("app_version") appVersion: Int,
    ): Response<AiTokensStateDto>

    @FormUrlEncoded
    @POST("shop/tokens/pre-buy")
    suspend fun preBuy(
        @Field("order_id") orderId: String,
        @Field("purchase_token") purchaseToken: String,
        @Field("product_id") productId: String,
        @Field("app_version") appVersion: Int,
    ): Response<AiTokensStateDto>


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