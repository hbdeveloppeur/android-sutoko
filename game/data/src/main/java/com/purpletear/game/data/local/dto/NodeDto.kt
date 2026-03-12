package com.purpletear.game.data.local.dto

import com.google.gson.annotations.SerializedName

data class NodeDto(
    val id: String,
    val type: String,
    val version: Int = 1,
    val position: PositionDto,
    val data: NodeDataDto
)

data class PositionDto(
    val x: Float,
    val y: Float
)

data class NodeDataDto(
    val label: String? = null,
    val text: String? = null,
    @SerializedName("characterId")
    val characterId: Int? = null,
    @SerializedName("chapterCode")
    val chapterCode: String? = null,
    val options: List<ChoiceOptionDto>? = null,
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
    val seen: Long? = null
)

data class ChoiceOptionDto(
    val text: String,
    @SerializedName("targetNodeId")
    val targetNodeId: String
)
