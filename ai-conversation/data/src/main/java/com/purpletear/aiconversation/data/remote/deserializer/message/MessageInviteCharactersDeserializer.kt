package com.purpletear.aiconversation.data.remote.deserializer.message

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.purpletear.aiconversation.data.remote.deserializer.AiCharacterDeserializer
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import com.purpletear.aiconversation.domain.model.messages.entities.MessageInviteCharacters
import java.lang.reflect.Type
import java.util.UUID

class MessageInviteCharactersDeserializer : JsonDeserializer<Message> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): MessageInviteCharacters {
        val jsonObject = json.asJsonObject

        val id = jsonObject.get("id")?.asString ?: UUID.randomUUID().toString()
        val timestamp = jsonObject.get("createdAt")?.asLong ?: (System.currentTimeMillis() / 1000)

        val characters = jsonObject.getAsJsonArray("characters")
        return MessageInviteCharacters(
            id = id,
            timestamp = timestamp,
            characters = characters.map {
                val character = it.asJsonObject
                val deserializer = AiCharacterDeserializer()
                deserializer.deserialize(character, typeOfT, context)
            },
        )
    }
}