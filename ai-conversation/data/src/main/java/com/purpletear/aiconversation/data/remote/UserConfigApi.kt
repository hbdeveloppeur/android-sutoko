package com.purpletear.aiconversation.data.remote

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UserConfigApi {

    @FormUrlEncoded
    @POST("config/update")
    suspend fun updateDeviceToken(
        @Field("userId") userId: String,
        @Field("token") userToken: String,
        @Field("configToken") deviceToken: String,
    ): Response<Unit>
}