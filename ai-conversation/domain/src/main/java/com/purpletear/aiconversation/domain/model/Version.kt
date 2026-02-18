package com.purpletear.aiconversation.domain.model

import androidx.annotation.Keep

@Keep
data class Version(
    val id: Int,
    val name: String,
    val code: String?,
    val createdAt: String,
    val releaseDate: Long,
    val online: String?,
    val backgroundUrl: String,
)

@Keep
data class VersionResponse(
    val current: Version,
    val next: Version
)