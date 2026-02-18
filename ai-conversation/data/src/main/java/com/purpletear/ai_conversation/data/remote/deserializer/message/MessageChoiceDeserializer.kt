package com.purpletear.ai_conversation.data.remote.deserializer.message

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.purpletear.ai_conversation.domain.model.messages.entities.Message
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageStoryChoice
import com.purpletear.ai_conversation.domain.model.messages.entities.MessageStoryChoiceGroup
import java.lang.reflect.Type
import java.util.UUID

class MessageChoiceDeserializer : JsonDeserializer<Message> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Message {
        val jsonObject = json.asJsonObject

        val id = jsonObject.get("id")?.asString ?: UUID.randomUUID().toString()
        val timestamp = jsonObject.get("createdAt")?.asLong ?: (System.currentTimeMillis() / 1000)
        val group = jsonObject.getAsJsonObject("choiceGroup")
        val choicesJson = group.getAsJsonArray("storyChoices")

        val choices = choicesJson.map { choiceElement ->
            val choiceObject = choiceElement.asJsonObject
            MessageStoryChoice(
                id = choiceObject.get("id").asString,
                text = choiceObject.get("text").asString,
                isSelected = choiceObject.get("selected").asBoolean,
            )
        }

        return MessageStoryChoiceGroup(
            id = id,
            timestamp = timestamp,
            isConsumed = (group.get("active")?.asBoolean == false),
            choices = choices
        )
    }
}