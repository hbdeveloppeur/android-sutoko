package com.purpletear.game.data.local.parser

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.purpletear.game.data.local.dto.ChapterMetadataDto
import com.purpletear.game.data.local.dto.NodeDto
import com.purpletear.sutoko.game.model.SUTOKO_MEDIA_BASE_URL
import com.purpletear.sutoko.game.model.chapter.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ChapterGraphParserVisualNovelTest {

    private val gson = Gson()

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val pathProvider by lazy { FakeGamePathProvider(tempFolder.root.absolutePath) }

    private fun gameAssetsDir(): File =
        File(tempFolder.root, "game1/assets").apply { mkdirs() }

    @Test
    fun `visual-novel node is parsed with resolved paths, ms durations and theme`() {
        // Media bundled in the archive resolves to local file paths.
        gameAssetsDir().also { dir ->
            File(dir, "bg.webp").createNewFile()
            File(dir, "rain.mp4").createNewFile()
            File(dir, "wind.mp3").createNewFile()
            File(dir, "voice1.mp3").createNewFile()
        }
        val assetsPath = gameAssetsDir().absolutePath
        val data = gson.fromJson(
            """{
              "title":"Inconnue 🍭",
              "layers":[
                {"assetId":11,"storagePath":"./bg.webp","type":"image"},
                {"assetId":12,"storagePath":"./rain.mp4","type":"video"}
              ],
              "dialogs":[
                {"text":"Hé, vous faites quoi ?","duration":2430,"delay":1000,
                 "soundAssetId":31,"soundStoragePath":"./voice1.mp3","soundName":"1.mp3",
                 "soundDurationMs":2430},
                {"text":"Tu m'écoutes ?","duration":null}
              ],
              "sounds":[
                {"assetId":21,"storagePath":"./wind.mp3","volume":0.6,"loop":true},
                {"assetId":22,"storagePath":"./thunder","volume":null,"loop":null}
              ],
              "theme":{"color":"#CD40CD","opacity":0.8},
              "delay":1250
            }""",
            JsonObject::class.java
        )

        val graph = parse("vn-1", data)

        val node = graph.getNode("vn-1") as Node.VisualNovel
        assertEquals("Inconnue 🍭", node.title)
        assertEquals(1250L, node.delayMs)

        assertEquals(2, node.layers.size)
        assertEquals("$assetsPath/bg.webp", node.layers[0].path)
        assertEquals(11, node.layers[0].assetId)
        assertFalse(node.layers[0].isVideo)
        assertEquals("$assetsPath/rain.mp4", node.layers[1].path)
        assertTrue(node.layers[1].isVideo)

        assertEquals(2, node.dialogs.size)
        assertEquals("Hé, vous faites quoi ?", node.dialogs[0].text)
        assertEquals(2430L, node.dialogs[0].durationMs)
        assertEquals(1000L, node.dialogs[0].delayMs)
        assertEquals("$assetsPath/voice1.mp3", node.dialogs[0].soundPath)
        assertEquals(null, node.dialogs[1].durationMs)
        assertEquals(0L, node.dialogs[1].delayMs)
        assertNull(node.dialogs[1].soundPath)

        assertEquals(2, node.sounds.size)
        assertEquals("$assetsPath/wind.mp3", node.sounds[0].path)
        assertEquals(0.6f, node.sounds[0].volume)
        assertTrue(node.sounds[0].loop)
        // Defaults: full volume, no loop.
        assertEquals(1f, node.sounds[1].volume)
        assertFalse(node.sounds[1].loop)

        assertEquals("#CD40CD", node.theme.colorHex)
        assertEquals(0.8f, node.theme.opacity)
    }

    @Test
    fun `visual-novel layers fall back to remote URL when not bundled in the archive`() {
        val data = gson.fromJson(
            """{
              "layers":[
                {"assetId":11,"storagePath":"uploads/images/2026/08/bg.webp","type":"image"},
                {"assetId":12,"storagePath":"uploads/videos/2026/08/rain.mp4","type":"video"}
              ]
            }""",
            JsonObject::class.java
        )

        val node = parse("vn-1", data).getNode("vn-1") as Node.VisualNovel

        assertEquals("${SUTOKO_MEDIA_BASE_URL}uploads/images/2026/08/bg.webp", node.layers[0].path)
        assertEquals("${SUTOKO_MEDIA_BASE_URL}uploads/videos/2026/08/rain.mp4", node.layers[1].path)
    }

    @Test
    fun `visual-novel sounds resolve to the bundled assets directory when missing`() {
        // Like every other sound node: always a local assets/ path, subdirectories stripped,
        // never a remote URL.
        val assetsPath = gameAssetsDir().absolutePath
        val data = gson.fromJson(
            """{
              "layers":[{"assetId":11,"storagePath":"./bg.webp","type":"image"}],
              "sounds":[
                {"assetId":21,"storagePath":"uploads/sounds/2026/08/wind.mp3","volume":1,"loop":false}
              ],
              "dialogs":[
                {"text":"Écoute ça","duration":1301,
                 "soundStoragePath":"uploads/sounds/2026/08/voice.mp3"}
              ]
            }""",
            JsonObject::class.java
        )

        val node = parse("vn-1", data).getNode("vn-1") as Node.VisualNovel

        assertEquals("$assetsPath/wind.mp3", node.sounds[0].path)
        assertEquals("$assetsPath/voice.mp3", node.dialogs[0].soundPath)
    }

    @Test
    fun `visual-novel sounds fall back to the legacy medias-sounds directory`() {
        val legacyDir = File(tempFolder.root, "game1/medias/sounds").apply { mkdirs() }
        File(legacyDir, "wind.mp3").createNewFile()
        File(legacyDir, "voice.mp3").createNewFile()
        val data = gson.fromJson(
            """{
              "layers":[{"assetId":11,"storagePath":"./bg.webp","type":"image"}],
              "sounds":[{"assetId":21,"storagePath":"./wind.mp3","volume":1,"loop":true}],
              "dialogs":[{"text":"Écoute ça","soundStoragePath":"./voice.mp3"}]
            }""",
            JsonObject::class.java
        )

        val node = parse("vn-1", data).getNode("vn-1") as Node.VisualNovel

        assertEquals(File(legacyDir, "wind.mp3").absolutePath, node.sounds[0].path)
        assertEquals(File(legacyDir, "voice.mp3").absolutePath, node.dialogs[0].soundPath)
    }

    @Test
    fun `sounded dialog without explicit duration falls back to the sound duration`() {
        val data = gson.fromJson(
            """{
              "layers":[{"storagePath":"./bg.webp","type":"image"}],
              "dialogs":[{"text":"Écoute","soundStoragePath":"./voice.mp3","soundDurationMs":1800}]
            }""",
            JsonObject::class.java
        )

        val node = parse("vn-1", data).getNode("vn-1") as Node.VisualNovel

        assertEquals(1800L, node.dialogs[0].durationMs)
    }

    @Test
    fun `visual-novel node falls back to the default theme when theme is missing or invalid`() {
        val missingTheme = gson.fromJson(
            """{"layers":[{"storagePath":"./bg.webp","type":"image"}]}""",
            JsonObject::class.java
        )
        val invalidTheme = gson.fromJson(
            """{
              "layers":[{"storagePath":"./bg.webp","type":"image"}],
              "theme":{"color":"violet","opacity":4.0}
            }""",
            JsonObject::class.java
        )

        val withoutTheme = parse("vn-1", missingTheme).getNode("vn-1") as Node.VisualNovel
        assertEquals("#332F63", withoutTheme.theme.colorHex)
        assertEquals(0.7f, withoutTheme.theme.opacity)

        val withInvalidTheme = parse("vn-2", invalidTheme).getNode("vn-2") as Node.VisualNovel
        assertEquals("#332F63", withInvalidTheme.theme.colorHex)
        // Out-of-range opacity is clamped, not dropped.
        assertEquals(1f, withInvalidTheme.theme.opacity)
    }

    @Test
    fun `visual-novel node is dropped when it has no usable layer`() {
        val data = gson.fromJson(
            """{"layers":[{"assetId":11,"type":"image"}],"dialogs":[{"text":"Hi","duration":2}]}""",
            JsonObject::class.java
        )

        val graph = parse("vn-1", data)

        assertNull(graph.getNode("vn-1"))
    }

    @Test
    fun `visual-novel parsing drops blank dialogs and caps volumes`() {
        val data = gson.fromJson(
            """{
              "layers":[{"storagePath":"./bg.webp","type":"image"}],
              "dialogs":[{"text":"   ","duration":2},{"text":"Salut","duration":-3}],
              "sounds":[{"storagePath":"./wind.mp3","volume":1.7}]
            }""",
            JsonObject::class.java
        )

        val node = parse("vn-1", data).getNode("vn-1") as Node.VisualNovel

        assertEquals(1, node.dialogs.size)
        assertEquals("Salut", node.dialogs[0].text)
        assertEquals(null, node.dialogs[0].durationMs)
        assertEquals(1f, node.sounds[0].volume)
    }

    private fun parse(nodeId: String, data: JsonElement) = ChapterGraphParser.parse(
        chapterCode = "1a",
        metadata = ChapterMetadataDto(title = "Chapter 1A"),
        nodeDtos = listOf(
            NodeDto(id = "start-0", type = "start", data = JsonObject()),
            NodeDto(id = nodeId, type = "visual-novel", data = data),
        ),
        edgeDtos = listOf(edge("start-0", nodeId)),
        gameId = "game1",
        legacyId = null,
        pathProvider = pathProvider
    )

}
