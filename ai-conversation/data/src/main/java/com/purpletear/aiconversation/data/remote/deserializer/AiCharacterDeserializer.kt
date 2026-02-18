package com.purpletear.aiconversation.data.remote.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.purpletear.aiconversation.data.exception.DeserializationException
import com.purpletear.aiconversation.data.extension.getStringValue
import com.purpletear.aiconversation.domain.enums.Visibility
import com.purpletear.aiconversation.domain.model.AiCharacter
import java.lang.reflect.Type

class AiCharacterDeserializer : JsonDeserializer<AiCharacter> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): AiCharacter {
        val jsonObject = json.asJsonObject

        val id = try {
            jsonObject.get("id").asInt
        } catch (e: Exception) {
            throw DeserializationException("Couldn't deserialize AiCharacter id", e)
        }

        val firstName = try {
            jsonObject.get("firstName").asString
        } catch (e: Exception) {
            throw DeserializationException("Couldn't deserialize AiCharacter firstName", e)
        }

        val lastName = jsonObject.getStringValue("lastName", null)

        val description = try {
            jsonObject.get("description").asString
        } catch (e: Exception) {
            throw DeserializationException("Couldn't deserialize AiCharacter description", e)
        }

        val avatarUrl = jsonObject.getStringValue("avatarUrl", null)

        val bannerUrl = jsonObject.getStringValue("bannerUrl", null)
        val code = jsonObject.getStringValue("code", null)

        val createdAt = try {
            jsonObject.get("createdAt").asLong
        } catch (e: Exception) {
            throw DeserializationException("Couldn't deserialize AiCharacter createdAt", e)
        }

        val visibility = try {
            val v = jsonObject.get("visibility").asString
            Visibility.fromString(v)
                ?: throw IllegalArgumentException("Visibility $v not found in enum Visibility")
        } catch (e: Exception) {
            throw DeserializationException("Couldn't deserialize AiCharacter visibility", e)
        }

        val statusDescription = jsonObject.getStringValue("statusDescription", null)

        return AiCharacter(
            id = id,
            firstName = firstName,
            lastName = lastName,
            description = description,
            avatarUrl = avatarUrl,
            bannerUrl = bannerUrl,
            createdAt = createdAt,
            visibility = visibility,
            statusDescription = statusDescription,
            code = code,

            )
    }
}