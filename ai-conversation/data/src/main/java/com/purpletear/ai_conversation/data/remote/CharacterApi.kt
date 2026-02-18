package com.purpletear.ai_conversation.data.remote

import com.purpletear.ai_conversation.data.remote.dto.AddCharacterDto
import com.purpletear.ai_conversation.data.remote.dto.AiCharacterDto
import com.purpletear.ai_conversation.data.remote.dto.AvatarBannerPairDto
import com.purpletear.ai_conversation.data.remote.dto.CharacterStatusDto
import com.purpletear.ai_conversation.data.remote.dto.CharacterWithStatusDto
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CharacterApi {

    @FormUrlEncoded
    @POST("character/assets/random")
    suspend fun getRandomAvatarAndBannerPair(
        @Field("isFemale") isFemale: Boolean,
        @Field("n") n: Int
    ): Response<AvatarBannerPairDto>

    @FormUrlEncoded
    @POST("character/add")
    suspend fun insertCharacter(
        @Field("userId") userId: String,
        @Field("token") token: String,
        @Field("firstName") firstName: String,
        @Field("lastName") lastName: String,
        @Field("gender") gender: String,
        @Field("description") description: String,
        @Field("avatarId") avatarId: Int?,
        @Field("bannerId") bannerId: Int?,
        @Field("styleId") styleId: Int,
        @Field("app_version") appVersion: String,
    ): Response<AddCharacterDto>

    @FormUrlEncoded
    @POST("character/delete")
    suspend fun deleteCharacter(
        @Field("userId") userId: String,
        @Field("token") token: String,
        @Field("aiCharacterId") aiCharacterId: Int,
        @Field("app_version") appVersion: String,
    ): Response<Unit>

    @FormUrlEncoded
    @POST("character/status")
    suspend fun getStatus(
        @Field("userId") userId: String,
        @Field("aiCharacterId") characterId: Int,
        @Field("app_version") appVersion: String
    ): Response<CharacterStatusDto>

    @FormUrlEncoded
    @POST("characters/status")
    suspend fun getAccessibleCharactersWithStatus(
        @Field("userId") userId: String,
        @Field("app_version") appVersion: String
    ): Response<List<CharacterWithStatusDto>>

    @FormUrlEncoded
    @POST("conversation/invite-characters")
    suspend fun inviteCharacters(
        @Field("userId") userId: String,
        @Field("aiCharacterId") characterId: Int,
        @Field("charactersId[]") charactersId: List<Int>,
        @Field("app_version") appVersion: String
    ): Response<Unit>

    @FormUrlEncoded
    @POST("characters")
    suspend fun getAll(
        @Field("userId") userId: String?,
        @Field("token") token: String?,
    ): Response<List<AiCharacterDto>>

    @FormUrlEncoded
    @POST("character")
    suspend fun get(
        @Field("userId") userId: String?,
        @Field("token") token: String?,
        @Field("characterId") characterId: Int,
    ): Response<AiCharacterDto>
}