package com.purpletear.aiconversation.data.remote.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.purpletear.aiconversation.domain.enums.MessageState
import java.io.IOException

class MessageStateTypeAdapter : TypeAdapter<MessageState>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: MessageState?) {
        out.value(value?.code)
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): MessageState {
        val value = `in`.nextString()
        return MessageState.fromString(value) ?: MessageState.Sent
    }
}
