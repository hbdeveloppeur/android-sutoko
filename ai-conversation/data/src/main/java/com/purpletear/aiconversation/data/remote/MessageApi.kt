package com.purpletear.aiconversation.data.remote

import com.purpletear.aiconversation.data.remote.dto.ConversationDto
import com.purpletear.aiconversation.data.remote.dto.SendMessageResponseDto
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MessageApi {
    @FormUrlEncoded
    @POST("send/message-media")
    suspend fun sendMessageImage(
        @Field("userId") userId: String?,
        @Field("token") token: String?,
        @Field("aiCharacterId") aiCharacterId: Int,
        @Field("mediaId") mediaId: Int,
        @Field("role") role: String,
        @Field("userName") userName: String?,
        @Field("timeZoneId") timeZoneId: String,
        @Field("langCode") langCode: String,
        @Field("app_version") appVersion: String
    ): Response<Unit>

    @FormUrlEncoded
    @POST("conversation/messages")
    suspend fun getAll(
        @Field("userId") userId: String?,
        @Field("token") token: String?,
        @Field("aiCharacterId") characterId: Int,
        @Field("app_version") appVersion: String,
    ): Response<List<Message>>

    @FormUrlEncoded
    @POST("save-fine-tuning")
    suspend fun saveForFineTune(
        @Field("userId") userId: String?,
    ): Response<Unit>

    @FormUrlEncoded
    @POST("conversation/settings")
    suspend fun getSettings(
        @Field("userId") userId: String?,
        @Field("aiCharacterId") characterId: Int,
        @Field("app_version") appVersion: String,
    ): Response<ConversationDto>

    @FormUrlEncoded
    @POST("restart-conversation")
    suspend fun restart(
        @Field("userId") userId: String?,
        @Field("aiCharacterId") characterId: Int,
        @Field("app_version") appVersion: String,
    ): Response<Unit>

    @Multipart
    @POST("send/message")
    suspend fun sendMessage(
        @Part("mrpId") mrpId: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("token") token: RequestBody,
        @Part("aiCharacterId") aiCharacterCode: RequestBody,
        @Part("app_version") appVersion: RequestBody,
        @Part("langCode") langCode: RequestBody,
        @Part("timeZoneId") timeZoneId: RequestBody,
        @Part("username") userName: RequestBody?,
        @Part texts: List<MultipartBody.Part>,
        @Part audioFiles: List<MultipartBody.Part>,
    ): Response<SendMessageResponseDto>
}