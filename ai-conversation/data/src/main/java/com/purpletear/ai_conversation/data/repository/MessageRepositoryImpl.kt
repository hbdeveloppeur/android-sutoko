package com.purpletear.ai_conversation.data.repository

import com.purpletear.ai_conversation.data.BuildConfig
import com.purpletear.ai_conversation.data.exception.ApiException
import com.purpletear.ai_conversation.data.remote.MessageApi
import com.purpletear.ai_conversation.data.remote.utils.ApiFailureResponseHandler
import com.purpletear.ai_conversation.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import purpletear.fr.purpleteartools.Language
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.util.TimeZone
import java.util.UUID

class MessageRepositoryImpl(
    private val api: MessageApi
) : MessageRepository {


    private fun getTimeZoneCode(): String {
        val timeZone = TimeZone.getDefault()
        return timeZone.id
    }

    override suspend fun sendMessage(
        uid: String,
        token: String,
        characterId: Int,
        texts: List<String>,
        audioFiles: List<File>,
        userName: String?
    ): Flow<Result<Unit>> = flow {
        try {
            if (texts.isEmpty() && audioFiles.isEmpty()) {
                emit(Result.success(Unit))
                return@flow
            }

            val uidRequestBody = uid.toRequestBody("text/plain".toMediaTypeOrNull())
            val mrpIdRequestBody =
                UUID.randomUUID().toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val tokenRequestBody = token.toRequestBody("text/plain".toMediaTypeOrNull())

            val langCode = (Language.determineLangDirectory()
                .take(2)).toRequestBody("text/plain".toMediaTypeOrNull())


            val timeZoneId = getTimeZoneCode().toRequestBody("text/plain".toMediaTypeOrNull())
            val aiCharacterCodeRequestBody =
                characterId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val versionRequestBody =
                BuildConfig.VERSION_NAME.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val userNameBody =
                userName?.toRequestBody("text/plain".toMediaTypeOrNull())

            val textParts: ArrayList<MultipartBody.Part> = ArrayList()

            texts.forEach { string ->
                val requestBody = string.toRequestBody("text/plain".toMediaTypeOrNull())
                textParts.add(MultipartBody.Part.createFormData("texts[]", null, requestBody))
            }


            val audioFilesMultipartBody: ArrayList<MultipartBody.Part> = ArrayList()
            audioFiles.forEach { file ->
                val filePath = file.path
                val tmpFile = File(filePath)
                val requestFile =
                    tmpFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                audioFilesMultipartBody.add(
                    MultipartBody.Part.createFormData("files[]", tmpFile.name, requestFile)
                )
            }

            val response = api.sendMessage(
                mrpId = mrpIdRequestBody,
                userId = uidRequestBody,
                token = tokenRequestBody,
                aiCharacterCode = aiCharacterCodeRequestBody,
                appVersion = versionRequestBody,
                langCode = langCode,
                timeZoneId = timeZoneId,
                userName = userNameBody,
                texts = textParts,
                audioFiles = audioFilesMultipartBody,
            )

            if (response.isSuccessful) {
                response.body()?.let {
                    val sendMessageResponse = it.toDomain()
                    emit(Result.success(Unit))
                } ?: emit(Result.failure(Exception("Response body is null")))
            } else {
                val exception: ApiException =
                    ApiFailureResponseHandler.handler(response.errorBody())
                emit(Result.failure(exception))
            }
        } catch (e: IOException) {
            emit(Result.failure(e))
        } catch (e: HttpException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(Exception("Error sending message", e)))
        }
    }.catch {
        emit(Result.failure(Exception("Error sending message", it)))
    }
}