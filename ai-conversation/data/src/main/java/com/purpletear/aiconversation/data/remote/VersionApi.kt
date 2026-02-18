package com.purpletear.aiconversation.data.remote

import com.purpletear.aiconversation.domain.model.VersionResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface VersionApi {

    @FormUrlEncoded
    @POST("version")
    suspend fun getVersion(
        @Field("app_version") appVersion: String
    ): Response<VersionResponse>
}