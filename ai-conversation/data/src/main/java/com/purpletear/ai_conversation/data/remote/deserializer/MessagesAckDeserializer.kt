package com.purpletear.ai_conversation.data.remote.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class MessagesAckDeserializer : JsonDeserializer<List<String>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<String> {
        val jsonObject = json.asJsonObject
        val jsonArray = jsonObject.getAsJsonArray("serialIds")
        return jsonArray.map { it.asString }
    }
}