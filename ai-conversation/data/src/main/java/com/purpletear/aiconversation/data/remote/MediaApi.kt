package com.purpletear.aiconversation.data.remote

import com.purpletear.aiconversation.data.remote.dto.SingleStringResultDto
import com.purpletear.aiconversation.data.remote.dto.UploadMediaResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MediaApi {

    @Multipart
    @POST("upload-media")
    suspend fun uploadMedia(
        @Part("userId") userId: RequestBody,
        @Part("token") token: RequestBody,
        @Part file: MultipartBody.Part,
    ): Response<UploadMediaResponseDto>

    @FormUrlEncoded
    @POST("media/describe")
    suspend fun describeMedia(
        @Field("mediaId") mediaId: Int,
        @Field("userId") userId: String?,
        @Field("langCode") langCode: String?,
        @Field("app_version") appVersion: String?
    ): Response<SingleStringResultDto>

}