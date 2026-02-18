package com.purpletear.aiconversation.data.remote.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.purpletear.aiconversation.data.exception.NotEnoughCoinsException
import com.purpletear.aiconversation.data.exception.UserNameNotFoundException
import com.purpletear.aiconversation.data.extension.getStringValue
import com.purpletear.aiconversation.domain.exception.WebsocketMessageParserException
import java.lang.reflect.Type

class ErrorCodeDeserializer : JsonDeserializer<Exception> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Exception {
        val jsonObject = json.asJsonObject

        val code = jsonObject.getStringValue("code", null)

        when (code) {
            "username_required" -> {
                return UserNameNotFoundException()
            }

            "insufficient_funds" -> {
                return NotEnoughCoinsException()
            }
        }

        throw WebsocketMessageParserException()
    }
}