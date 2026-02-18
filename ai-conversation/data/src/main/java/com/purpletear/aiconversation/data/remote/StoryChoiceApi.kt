package com.purpletear.aiconversation.data.remote

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface StoryChoiceApi {
    @FormUrlEncoded
    @POST("story/choice/enable")
    suspend fun makeChoice(
        @Field("userId") userId: String?,
        @Field("choiceId") choiceId: String?,
        @Field("app_version") appVersion: String,
    ): Response<Unit>
}