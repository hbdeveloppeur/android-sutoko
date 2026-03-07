package com.purpletear.game.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.purpletear.sutoko.game.model.Asset
import com.purpletear.sutoko.game.model.Author
import com.purpletear.sutoko.game.model.GameMetadata

class GameTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromAsset(asset: Asset?): String? {
        return asset?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toAsset(value: String?): Asset? {
        return value?.let {
            val type = object : TypeToken<Asset>() {}.type
            gson.fromJson(it, type)
        }
    }

    @TypeConverter
    fun fromGameMetadata(metadata: GameMetadata): String {
        return gson.toJson(metadata)
    }

    @TypeConverter
    fun toGameMetadata(value: String): GameMetadata {
        val type = object : TypeToken<GameMetadata>() {}.type
        return gson.fromJson(value, type) ?: GameMetadata("")
    }

    @TypeConverter
    fun fromAuthor(author: Author?): String? {
        return author?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toAuthor(value: String?): Author? {
        return value?.let {
            val type = object : TypeToken<Author>() {}.type
            gson.fromJson(it, type)
        }
    }
}
