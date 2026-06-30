package com.purpletear.game.data.remote.testing.dto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class TestPackageManifestDtoTest {

    private val gson = Gson()

    @Test
    fun `parseManifest accepts assetInventory as list of objects`() {
        val json = """
            {
                "seed": 28,
                "chapterId": "01KVNC59066Q2Y3TQ65ZBTA8VW",
                "storyId": "story-1",
                "updatedAt": "2024-01-01T00:00:00Z",
                "nodes": [],
                "edges": [],
                "assetInventory": [
                    { "uniqueFileName": "17918a87-6b56-432f-bbf5-32d7536e7d3d.webp" }
                ]
            }
        """.trimIndent()

        val manifest = gson.parseManifest(json)

        assertEquals(28, manifest.seed)
        assertEquals("01KVNC59066Q2Y3TQ65ZBTA8VW", manifest.chapterId)
        assertEquals(1, manifest.assetInventory.size)
        assertEquals(
            "17918a87-6b56-432f-bbf5-32d7536e7d3d.webp",
            manifest.assetInventory.first().uniqueFileName
        )
    }

    @Test
    fun `parseManifest unwraps manifest from single-element array`() {
        val json = """
            [{
                "seed": 28,
                "chapterId": "01KVNC59066Q2Y3TQ65ZBTA8VW",
                "storyId": "story-1",
                "updatedAt": "2024-01-01T00:00:00Z",
                "nodes": [],
                "edges": [],
                "assetInventory": [
                    { "uniqueFileName": "image.webp" }
                ]
            }]
        """.trimIndent()

        val manifest = gson.parseManifest(json)

        assertEquals(28, manifest.seed)
        assertEquals("image.webp", manifest.assetInventory.first().uniqueFileName)
    }
}
