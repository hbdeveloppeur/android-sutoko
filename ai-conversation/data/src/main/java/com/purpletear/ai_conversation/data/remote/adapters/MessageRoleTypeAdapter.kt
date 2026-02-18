package com.purpletear.ai_conversation.data.remote.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.purpletear.ai_conversation.domain.enums.MessageRole
import java.io.IOException

class MessageRoleTypeAdapter : TypeAdapter<MessageRole>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: MessageRole?) {
        out.value(value?.code)
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): MessageRole {
        val value = `in`.nextString()
        return MessageRole.fromString(value) ?: MessageRole.User
    }
}
