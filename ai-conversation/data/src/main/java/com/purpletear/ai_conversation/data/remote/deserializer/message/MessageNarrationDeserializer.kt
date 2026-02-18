package com.purpletear.ai_conversation.data.remote.deserializer.message

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.purpletear.ai_conversation.domain.enums.MessageState
import com.purpletear.ai_conversation.domain.model.messages.entities.Message
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageNarration
import java.lang.reflect.Type
import java.util.UUID

class MessageNarrationDeserializer : JsonDeserializer<Message> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Message {
        val jsonObject = json.asJsonObject

        val id = jsonObject.get("id")?.asString ?: UUID.randomUUID().toString()
        val timestamp = jsonObject.get("createdAt")?.asLong ?: System.currentTimeMillis()
        val text = jsonObject.get("text")?.asString ?: ""

        return MessageNarration(
            text = text,
            id = id,
            hiddenState = MessageState.Idle,
            timestamp = timestamp,
        )
    }
}