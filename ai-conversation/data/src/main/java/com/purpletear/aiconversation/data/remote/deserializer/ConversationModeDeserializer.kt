package com.purpletear.aiconversation.data.remote.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.purpletear.aiconversation.domain.enums.ConversationMode
import com.purpletear.aiconversation.domain.exception.WebsocketMessageParserException
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