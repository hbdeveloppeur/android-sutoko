package com.purpletear.aiconversation.data.remote.deserializer.message

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.purpletear.aiconversation.domain.exception.WebsocketMessageParserException
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import java.lang.reflect.Type

class MessageDeserializer : JsonDeserializer<Message> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Message {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type")?.asString ?: "text"


        val deserializers = mapOf(
            "text" to MessageTextDeserializer(),
            "story_telling" to MessageNarrationDeserializer(),
            "choice" to MessageChoiceDeserializer(),
            "invite_characters" to MessageInviteCharactersDeserializer(),
            "vocal" to MessageVocalDeserializer(),
        )


        when (type) {
            "media" -> {
                val media = jsonObject.getAsJsonObject("media")
                val mediaType = media.get("type")?.asString
                if (mediaType == "image") {
                    return MessageImageDeserializer().deserialize(json, typeOfT, context)
                }
            }

            else -> {
                deserializers[type]?.let { deserializer ->
                    return deserializer.deserialize(json, typeOfT, context)
                }
            }
        }

        throw WebsocketMessageParserException("Unknown message type: $type")
    }
}