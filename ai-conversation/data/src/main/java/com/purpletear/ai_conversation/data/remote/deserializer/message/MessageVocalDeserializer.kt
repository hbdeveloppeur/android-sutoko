package com.purpletear.ai_conversation.data.remote.deserializer.message

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.purpletear.ai_conversation.domain.enums.MessageRole
import com.purpletear.ai_conversation.domain.enums.MessageState
import com.purpletear.ai_conversation.domain.model.messages.entities.Message
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageVocal
import java.lang.reflect.Type
import java.util.UUID

class MessageVocalDeserializer : JsonDeserializer<Message> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Message {
        val jsonObject = json.asJsonObject

        val id = jsonObject.get("id")?.asString ?: UUID.randomUUID().toString()
        val timestamp = jsonObject.get("createdAt")?.asLong ?: System.currentTimeMillis()
        val role = jsonObject.get("role")?.asString
        val state = jsonObject.get("state")?.asString

        return MessageVocal(
            id = id,
            timestamp = timestamp,
            role = MessageRole.fromString(role) ?: MessageRole.User,
            state = MessageState.fromString(state) ?: MessageState.Seen,
        )
    }
}