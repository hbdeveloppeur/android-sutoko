package com.purpletear.aiconversation.data.remote.deserializer.message

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.purpletear.aiconversation.data.extension.getIntValue
import com.purpletear.aiconversation.data.extension.getLongValue
import com.purpletear.aiconversation.data.extension.getStringValue
import com.purpletear.aiconversation.domain.enums.MessageRole
import com.purpletear.aiconversation.domain.enums.MessageState
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import com.purpletear.aiconversation.domain.model.messages.entities.MessageImage
import java.lang.reflect.Type
import java.util.UUID

class MessageImageDeserializer : JsonDeserializer<Message> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Message {
        val jsonObject = json.asJsonObject


        val media = jsonObject.getAsJsonObject("media")

        val id = jsonObject.getStringValue("id", UUID.randomUUID().toString())!!
        val timestamp = jsonObject.getLongValue("createdAt", (System.currentTimeMillis() / 1000))!!
        val aiCharacterId = jsonObject.getIntValue("aiCharacterId", null)
        val url = media.getStringValue("url", "")!!
        val role = media.getStringValue("role", null)
        val description = media.getStringValue("description", null)

        return MessageImage(
            id = id,
            timestamp = timestamp,
            role = MessageRole.fromString(role) ?: MessageRole.Narrator,
            state = MessageState.Sent,
            url = url,
            width = jsonObject.get("width")?.asInt ?: 1024,
            height = jsonObject.get("height")?.asInt ?: 1024,
            aiCharacterId = aiCharacterId,
            hiddenState = MessageState.Idle,
            description = description,
        )
    }
}

