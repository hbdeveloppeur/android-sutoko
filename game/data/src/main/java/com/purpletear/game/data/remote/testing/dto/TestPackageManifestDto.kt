package com.purpletear.game.data.remote.testing.dto

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.purpletear.game.data.local.dto.EdgeDto
import com.purpletear.game.data.local.dto.NodeDto
import com.purpletear.sutoko.game.testing.StoryTestingLogger

@Keep
data class TestPackageManifestDto(
    val seed: Int,
    val chapterId: String,
    val storyId: String,
    val updatedAt: String,
    val nodes: List<NodeDto>,
    val edges: List<EdgeDto>,
    val assetInventory: List<AssetInventoryItemDto>,
)

@Keep
data class AssetInventoryItemDto(
    val uniqueFileName: String,
)

/**
 * Parses a manifest from JSON.
 *
 * The backend currently wraps some manifests in a single-element array, so this
 * helper accepts both a plain object and a non-empty array whose first element
 * is the manifest object. Any other shape throws a clear error.
 */
internal fun Gson.parseManifest(json: String): TestPackageManifestDto {
    val root = fromJson(json, JsonElement::class.java)
        ?: throw IllegalStateException("Manifest is empty or invalid JSON")

    val manifestObject: JsonObject = when {
        root.isJsonObject -> root.asJsonObject
        root.isJsonArray && root.asJsonArray.size() > 0 -> {
            val first = root.asJsonArray.get(0)
            if (!first.isJsonObject) {
                throw IllegalStateException("Manifest array first element is not an object")
            }
            StoryTestingLogger.d("PKG") { "Unwrapped manifest from single-element array" }
            first.asJsonObject
        }

        else -> throw IllegalStateException(
            "Manifest must be a JSON object or a non-empty array, got ${root.javaClass.simpleName}"
        )
    }

    return fromJson(manifestObject, TestPackageManifestDto::class.java)
}
