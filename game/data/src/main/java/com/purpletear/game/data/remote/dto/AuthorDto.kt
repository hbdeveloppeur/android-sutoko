package com.purpletear.game.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.purpletear.sutoko.game.model.Author

@Keep
data class AuthorDto(
    @SerializedName("displayName") val displayName: String,
    @SerializedName("avatarUrl") val avatarUrl: String?,
)

fun AuthorDto.toDomain(): Author {
    return Author(
        displayName = displayName,
        avatarUrl = avatarUrl
    )
}
