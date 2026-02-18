package com.purpletear.aiconversation.data.remote.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.purpletear.aiconversation.domain.model.AiCharacter
import java.lang.reflect.Type

class WebsocketCharactersDeserializer : JsonDeserializer<List<AiCharacter>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<AiCharacter> {
        val jsonObject = json.asJsonObject

        val characters = jsonObject.getAsJsonArray("characters")
        return characters.map {
            val character = it.asJsonObject
            val deserializer = AiCharacterDeserializer()
            deserializer.deserialize(character, typeOfT, context)
        }

    }
}