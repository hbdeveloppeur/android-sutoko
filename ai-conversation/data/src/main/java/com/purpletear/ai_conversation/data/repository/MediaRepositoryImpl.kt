package com.purpletear.ai_conversation.data.repository

import com.purpletear.ai_conversation.data.BuildConfig
import com.purpletear.ai_conversation.data.dao.MediaDao
import com.purpletear.ai_conversation.data.exception.NoBannerFoundException
import com.purpletear.ai_conversation.data.exception.NoResponseException
import com.purpletear.ai_conversation.data.remote.MediaApi
import com.purpletear.ai_conversation.data.remote.dto.toDomain
import com.purpletear.ai_conversation.data.remote.utils.ApiFailureResponseHandler
import com.purpletear.ai_conversation.domain.enums.MediaType
import com.purpletear.ai_conversation.domain.model.AvatarBannerPair
import com.purpletear.ai_conversation.domain.model.ImageGenerationRequest
import com.purpletear.ai_conversation.domain.model.Media
import com.purpletear.ai_conversation.domain.repository.MediaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import purpletear.fr.purpleteartools.Language
import java.io.File

class MediaRepositoryImpl(private val dao: MediaDao, private val api: MediaApi) : MediaRepository {
    override suspend fun getMediasFromImageRequest(imageGenerationRequest: ImageGenerationRequest) =
        dao.getByImageRequest(imageGenerationRequest.serial)

    override suspend fun persist(media: Media) = dao.persist(media)
    override suspend fun uploadMedia(
        userId: String,
        userToken: String,
        file: File,
        type: MediaType,
    ): Flow<Result<Media>> = flow {

        val uidRequestBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())
        val tokenRequestBody = userToken.toRequestBody("text/plain".toMediaTypeOrNull())
        val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())

        val apiResponse = api.uploadMedia(
            userId = uidRequestBody,
            token = tokenRequestBody,
            file = MultipartBody.Part.createFormData("file", file.name, requestFile)
        )

        if (apiResponse.isSuccessful) {
            apiResponse.body()?.let { response ->
                delay(280L)
                file.delete()
                val media = response.toDomain(type)
                emit(Result.success(media))
            } ?: run {
                file.delete()
                emit(Result.failure(NoResponseException()))
                return@flow
            }
        } else {
            file.delete()
            val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
            emit(Result.failure(exception))
        }
    }.catch {
        emit(Result.failure(it))
    }

    override suspend fun describeMedia(userId: String, mediaId: Int): Flow<Result<String>> = flow {
        val apiResponse = api.describeMedia(
            mediaId = mediaId,
            langCode = (Language.determineLangDirectory()
                .take(2)),
            userId = userId,
            appVersion = BuildConfig.VERSION_NAME
        )

        if (apiResponse.isSuccessful) {
            apiResponse.body()?.let { response ->
                emit(Result.success(response.result))
            } ?: run {
                emit(Result.failure(NoResponseException()))
                return@flow
            }
        } else {
            val exception = ApiFailureResponseHandler.handler(apiResponse.errorBody())
            exception.printStackTrace()
        }
    }.catch {
        emit(Result.failure(it))
    }

    override suspend fun getAvatarAndBanner(imageGenerationRequestSerialId: String): Flow<Result<AvatarBannerPair>> =
        flow {
            dao.getByImageRequest(imageGenerationRequestSerialId).collect { medias ->
                if (medias.isEmpty()) {
                    // TODO : get remotely

                } else {
                    val banner: Media = medias.firstOrNull { it.typeCode == MediaType.Banner.code }
                        ?: throw NoBannerFoundException()
                    val avatar: Media? = medias.firstOrNull { it.typeCode == MediaType.Avatar.code }
                    val pair = AvatarBannerPair(
                        avatar = avatar,
                        banner = banner
                    )
                    emit(Result.success(pair))
                }
            }
        }.catch {
            emit(Result.failure(it))
        }
}