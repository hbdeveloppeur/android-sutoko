package com.purpletear.game.data.database

import androidx.room.TypeConverter
import com.google.gson.*
import com.purpletear.sutoko.game.engine.GameCheckpoint
import com.purpletear.sutoko.game.engine.GameMessage
import com.purpletear.sutoko.game.engine.GameMessageType
import com.purpletear.sutoko.game.engine.message.*
import java.lang.reflect.Type

class GameCheckpointConverters {
    private val gson = GsonBuilder().registerTypeAdapter(GameMessage::class.java, MessageAdapter()).create()

    @TypeConverter
    fun encode(checkpoint: GameCheckpoint?): String? = checkpoint?.let { gson.toJson(it) }

    @TypeConverter
    fun decode(json: String?): GameCheckpoint? = json?.let {
        try {
            gson.fromJson(it, GameCheckpoint::class.java)
        } catch (_: RuntimeException) {
            null // A damaged save must not prevent the progress/catalog database from opening.
        }
    }

    private class MessageAdapter : JsonSerializer<GameMessage>, JsonDeserializer<GameMessage> {
        override fun serialize(message: GameMessage, type: Type, context: JsonSerializationContext): JsonElement =
            context.serialize(message, message.javaClass)

        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): GameMessage {
            val value = json.asJsonObject
            requireString(value, "id")
            val messageType = try {
                GameMessageType.valueOf(requireString(value, "type"))
            } catch (e: IllegalArgumentException) {
                throw JsonParseException("Unknown saved message type", e)
            }
            val target = when (messageType) {
                GameMessageType.Text -> GameMessageText::class.java
                GameMessageType.Info -> GameMessageInfo::class.java
                GameMessageType.Image -> GameMessageImage::class.java
                GameMessageType.Vocal -> GameMessageVocal::class.java
                GameMessageType.AudioDialogue -> GameMessageAudioDialogue::class.java
                GameMessageType.MangaPage -> GameMessageMangaPage::class.java
                GameMessageType.ChapterEnd -> GameMessageNextChapter::class.java
                GameMessageType.Typing -> throw JsonParseException("Typing is not a stable checkpoint message")
            }
            when (messageType) {
                GameMessageType.Text, GameMessageType.Info -> requireString(value, "text")
                GameMessageType.Image, GameMessageType.MangaPage -> requireString(value, "imageUrl")
                GameMessageType.Vocal, GameMessageType.AudioDialogue -> requireString(value, "audioUrl")
                else -> Unit
            }
            if (messageType == GameMessageType.AudioDialogue) requireString(value, "text")
            if (messageType == GameMessageType.MangaPage && value.get("overlays")?.isJsonArray != true) {
                throw JsonParseException("Invalid saved manga overlays")
            }
            return context.deserialize(json, target)
        }

        private fun requireString(value: JsonObject, key: String): String {
            val field = value.get(key)
            if (field == null || !field.isJsonPrimitive || !field.asJsonPrimitive.isString) {
                throw JsonParseException("Missing saved message field: $key")
            }
            return field.asString
        }
    }
}
