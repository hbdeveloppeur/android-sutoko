package com.purpletear.news_data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.purpletear.sutoko.core.domain.appaction.ActionName
import com.purpletear.sutoko.core.domain.appaction.AppAction
import com.purpletear.sutoko.news.model.InvalidActionNameException

@Keep
data class ActionDto(
    @SerializedName("name") val name: String?,
    @SerializedName("value") val value: String?,
)

/**
 * Converts an instance of [ActionDto] to its corresponding domain model [AppAction].
 *
 * If the `name` property in [ActionDto] cannot be resolved to a valid [ActionName],
 * the function returns null.
 *
 * @return A domain model [AppAction] instance if the conversion is successful,
 * or null if the `name` property is invalid.
 */
fun ActionDto.toDomain(): AppAction? {
    try {
        val nameEnum = ActionName.fromStringOrThrow(name)
        return AppAction(name = nameEnum, value = value)
    } catch (e: InvalidActionNameException) {
        return null
    }
}
