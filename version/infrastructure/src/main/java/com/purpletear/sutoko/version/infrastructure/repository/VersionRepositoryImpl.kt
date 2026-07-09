package com.purpletear.sutoko.version.infrastructure.repository

import com.purpletear.sutoko.version.infrastructure.remote.VersionApi
import com.purpletear.sutoko.version.infrastructure.remote.dto.VersionByNameRequestDto
import com.purpletear.sutoko.version.infrastructure.remote.dto.toDomain
import com.purpletear.sutoko.version.model.Version
import com.purpletear.sutoko.version.repository.VersionRepository
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class VersionRepositoryImpl(
    private val api: VersionApi
) : VersionRepository {

    override suspend fun getVersionByName(name: String, languageCode: String): Result<Version> = try {
        val response = api.getVersionByName(
            VersionByNameRequestDto(name = name, languageCode = languageCode)
        )
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        val body = response.body() ?: throw IllegalStateException("Empty body for getVersionByName(name=$name, languageCode=$languageCode)")
        Result.success(body.toDomain())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
