package com.purpletear.aiconversation.data.remote.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.purpletear.aiconversation.domain.enums.CharacterStatus
import com.purpletear.aiconversation.domain.exception.WebsocketMessageParserException
import java.lang.reflect.Type

class CharacterStatusDeserializer : JsonDeserializer<CharacterStatus> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): CharacterStatus {
        try {
            val jsonObject = json.asJsonObject

            return CharacterStatus.valueOf(jsonObject.get("statusCode").asString)
        } catch (e: Exception) {
            throw WebsocketMessageParserException("Failed to parse MessageStoryChoiceGroup", e)
        }
    }
}