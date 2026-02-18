package com.purpletear.aiconversation.data.remote

import com.purpletear.aiconversation.data.remote.dto.GetAllDocumentsDto
import com.purpletear.aiconversation.data.remote.dto.InsertImageGenerationRequestDto
import com.purpletear.aiconversation.domain.model.ImageGeneratorSettings
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ImageGenerationApi {
    @FormUrlEncoded
    @POST("user/documents/image-requests")
    suspend fun getAllRequests(
        @Field("userId") userId: String,
        @Field("token") token: String,
        @Field("app_version") appVersion: String,
    ): Response<GetAllDocumentsDto>

    @FormUrlEncoded
    @POST("generate-image")
    suspend fun sendImageGenerationRequest(
        @Field("userId") userId: String,
        @Field("token") token: String,
        @Field("modelName") modelName: String,
        @Field("prompt") prompt: String,
        @Field("imageRequestSerialId") imageRequestSerialId: String,
        @Field("documentSerialId") documentSerialId: String,
        @Field("app_version") appVersion: String,
    ): Response<InsertImageGenerationRequestDto>

    @FormUrlEncoded
    @POST("delete-image")
    suspend fun deleteImageGenerationRequest(
        @Field("userId") userId: String,
        @Field("token") token: String,
        @Field("imageRequestSerialId") imageRequestSerialId: String,
        @Field("app_version") appVersion: String,
    ): Response<Unit>

    @FormUrlEncoded
    @POST("image-generator/settings")
    suspend fun getSettings(
        @Field("app_version") appVersion: String,
    ): Response<ImageGeneratorSettings>
}