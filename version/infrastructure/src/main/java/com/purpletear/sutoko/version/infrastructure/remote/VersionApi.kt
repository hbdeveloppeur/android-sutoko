package com.purpletear.sutoko.version.infrastructure.remote

import com.purpletear.sutoko.version.infrastructure.remote.dto.VersionDto
import com.purpletear.sutoko.version.infrastructure.remote.dto.VersionByNameRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface VersionApi {
    @POST("version/by-name")
    suspend fun getVersionByName(@Body body: VersionByNameRequestDto): Response<VersionDto>
}
