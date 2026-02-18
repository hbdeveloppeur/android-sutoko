package com.purpletear.sutoko.version.repository

import com.purpletear.sutoko.version.model.Version

interface VersionRepository {
    suspend fun getVersionByName(name: String, languageCode: String): Result<Version>
}
