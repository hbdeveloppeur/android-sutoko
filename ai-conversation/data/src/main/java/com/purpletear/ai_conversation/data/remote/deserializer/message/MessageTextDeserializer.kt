package com.purpletear.ai_conversation.data.remote.deserializer.message

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.purpletear.ai_conversation.domain.enums.MessageRole
import com.purpletear.ai_conversation.domain.enums.MessageState
import com.purpletear.ai_conversation.domain.model.messages.entities.Message
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageText
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageTyping
import java.lang.reflect.Type
import java.util.UUID

class MessageTextDeserializer : JsonDeserializer<Message> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Message {
        val jsonObject = json.asJsonObject

        val id = jsonObject.get("id")?.asString ?: UUID.randomUUID().toString()
        val timestamp = jsonObject.get("createdAt")?.asLong ?: (System.currentTimeMillis() / 1000)
        val typingDuration = jsonObject.get("typingDuration")?.asInt ?: 0
        val text = jsonObject.get("text")?.asString ?: ""
        val aiCharacterId = jsonObject.get("aiCharacterId")?.asInt
        val role = jsonObject.get("role")?.asString
        val state = jsonObject.get("state")?.asString

        return MessageText(
            text = text,
            id = id,
            timestamp = timestamp,
            role = MessageRole.fromString(role) ?: MessageRole.Assistant,
            state = MessageState.fromString(state) ?: MessageState.Seen,
            typing = MessageTyping(typingDuration, 0),
            hiddenState = MessageState.Idle,
            aiCharacterId = aiCharacterId,
        )
    }
}