package com.purpletear.game.data.local.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class NodeDto(
    val id: String,
    val type: String,
    val version: Int = 1,
    val data: JsonElement?
)

data class NodeDataDto(
    val label: String? = null,
    val text: String? = null,
    @SerializedName("characterId")
    val characterId: Int? = null,
    @SerializedName("chapterCode")
    val chapterCode: String? = null,
    val expression: String? = null,
    @SerializedName("trueTargetId")
    val trueTargetId: String? = null,
    @SerializedName("falseTargetId")
    val falseTargetId: String? = null,
    val key: String? = null,
    val value: String? = null,
    @SerializedName("trophyId")
    val trophyId: String? = null,
    val action: String? = null,
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    val wait: Long? = null,
    val seen: Long? = null,
    @SerializedName("sceneId")
    val sceneId: Int? = null,
    @SerializedName("storagePath")
    val storagePath: String? = null,
    val image: String? = null,
    @SerializedName("assetId")
    val assetId: Int? = null,
    val memory: MemoryDataDto? = null,
    @SerializedName("expectedValue")
    val expectedValue: String? = null,
)

data class MemoryDataDto(
    val id: String? = null,
    @SerializedName("chapterId")
    val chapterId: String? = null,
    val name: String? = null,
    val value: String? = null,
    val description: String? = null,
)