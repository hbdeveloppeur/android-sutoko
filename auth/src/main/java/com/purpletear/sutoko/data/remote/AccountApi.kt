package com.purpletear.sutoko.data.remote

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AccountApi {

    @FormUrlEncoded
    @POST("request-account-deletion")
    suspend fun requestAccountDeletion(
        @Field("userId") userId: String,
    ): Response<Unit>
}
