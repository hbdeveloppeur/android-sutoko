package com.purpletear.aiconversation.data.remote.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.purpletear.aiconversation.domain.exception.WebsocketMessageParserException
import com.purpletear.aiconversation.domain.model.messages.entities.Message
import com.purpletear.aiconversation.domain.model.messages.entities.MessageStoryChoice
import com.purpletear.aiconversation.domain.model.messages.entities.MessageStoryChoiceGroup
import java.lang.reflect.Type

class MessageStoryChoiceGroupDeserializer : JsonDeserializer<Message> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Message {
        try {
            val jsonObject = json.asJsonObject

            // Extract the group id from the JSON
            val groupId = jsonObject.get("id").asString

            // Extract the timestamp from the JSON
            val timestamp = jsonObject.get("createdAt").asLong

            // Extract the choices array
            val choicesJsonArray = jsonObject.getAsJsonArray("choices")
            val choices = choicesJsonArray.map { choiceElement ->
                val choiceObject = choiceElement.asJsonObject
                val id = choiceObject.get("id").asString
                val text = choiceObject.get("text").asString
                MessageStoryChoice(id, text)
            }

            // Return an instance of MessageStoryChoiceGroup
            return MessageStoryChoiceGroup(
                id = groupId,
                timestamp = timestamp,
                choices = choices
            )
        } catch (e: Exception) {
            throw WebsocketMessageParserException("Failed to parse MessageStoryChoiceGroup", e)
        }
    }
}