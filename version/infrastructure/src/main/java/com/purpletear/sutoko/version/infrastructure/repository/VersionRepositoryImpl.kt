package com.purpletear.sutoko.version.infrastructure.repository

import com.purpletear.sutoko.version.infrastructure.remote.VersionApi
import com.purpletear.sutoko.version.infrastructure.remote.dto.VersionByNameRequestDto
import com.purpletear.sutoko.version.infrastructure.remote.dto.toDomain
import com.purpletear.sutoko.version.model.Version
import com.purpletear.sutoko.version.repository.VersionRepository
import retrofit2.HttpException

class VersionRepositoryImpl(
    private val api: VersionApi
) : VersionRepository {

    override suspend fun getVersionByName(name: String, languageCode: String): Result<Version> = runCatching {
        val response = api.getVersionByName(
            VersionByNameRequestDto(name = name, languageCode = languageCode)
        )
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
        val body = response.body() ?: throw IllegalStateException("Empty body for getVersionByName(name=$name, languageCode=$languageCode)")
        body.toDomain()
    }
}
