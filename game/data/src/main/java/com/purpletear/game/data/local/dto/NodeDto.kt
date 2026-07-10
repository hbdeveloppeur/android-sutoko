package com.purpletear.game.data.local.dto

import androidx.annotation.Keep
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

@Keep
data class NodeDto(
    val id: String,
    val type: String,
    val version: Int = 1,
    val data: JsonElement?
)

@Keep
data class NodeDataDto(
    val label: String? = null,
    val text: String? = null,
    @SerializedName("characterId")
    val characterId: Int? = null,
    @SerializedName("chapterCode")
    val chapterCode: String? = null,
    val expression: String? = null,
    val key: String? = null,
    val value: String? = null,
    @SerializedName("trophyId")
    val trophyId: String? = null,
    val action: String? = null,
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    val wait: Long? = null,
    val seen: Long? = null,
    @SerializedName("isHesitating")
    val isHesitating: Boolean? = null,
    @SerializedName("sceneId")
    val sceneId: Int? = null,
    @SerializedName("storagePath")
    val storagePath: String? = null,
    val image: String? = null,
    @SerializedName("assetId")
    val assetId: Int? = null,
    @SerializedName("assetName")
    val assetName: String? = null,
    val memory: MemoryDto? = null,
    @SerializedName("expectedValue")
    val expectedValue: String? = null,
    @SerializedName("isLooping")
    val isLooping: Boolean? = null,
    val alignment: String? = null,
    val delay: Long? = null,
    val duration: Long? = null,
    @SerializedName("backgroundColor")
    val backgroundColor: String? = null,
    @SerializedName("foregroundColor")
    val foregroundColor: String? = null,
)

@Keep
data class MemoryDto(
    @SerializedName("name")
    val key: String,
    val value: String,
    val chapterId: String,
)