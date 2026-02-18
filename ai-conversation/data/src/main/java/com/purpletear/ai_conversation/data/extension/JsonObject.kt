package com.purpletear.ai_conversation.data.extension

import com.google.gson.JsonObject

fun JsonObject?.getIntValue(key: String, defaultValue: Int?): Int? {
    if (this == null) return null
    val e = this.get(key)
    return if (e != null && !e.isJsonNull && e.isJsonPrimitive) {
        e.asInt
    } else {
        defaultValue
    }
}


fun JsonObject?.getLongValue(key: String, defaultValue: Long?): Long? {
    if (this == null) return null
    val e = this.get(key)
    return if (e != null && !e.isJsonNull && e.isJsonPrimitive) {
        e.asLong
    } else {
        defaultValue
    }
}


fun JsonObject?.getStringValue(key: String, defaultValue: String?): String? {
    if (this == null) return null
    val e = this.get(key)
    return if (e != null && !e.isJsonNull && e.isJsonPrimitive) {
        e.asString
    } else {
        defaultValue
    }
}