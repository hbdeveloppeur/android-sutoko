package com.purpletear.ai_conversation.data.remote.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.purpletear.ai_conversation.domain.enums.ConversationMode
import com.purpletear.ai_conversation.domain.exception.WebsocketMessageParserException
import java.lang.reflect.Type

class ConversationModeDeserializer : JsonDeserializer<ConversationMode> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ConversationMode {
        try {
            val jsonObject = json.asJsonObject

            return ConversationMode.valueOf(jsonObject.get("mode").asString)
        } catch (e: Exception) {
            throw WebsocketMessageParserException("Failed to parse ConversationMode", e)
        }
    }
}