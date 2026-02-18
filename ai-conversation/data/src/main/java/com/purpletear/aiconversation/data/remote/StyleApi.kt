package com.purpletear.aiconversation.data.remote

import com.purpletear.aiconversation.domain.model.Style
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface StyleApi {

    @FormUrlEncoded
    @POST("styles")
    suspend fun getAllStyles(
        @Field("locale") locale: String,
    ): Response<List<Style>>
}