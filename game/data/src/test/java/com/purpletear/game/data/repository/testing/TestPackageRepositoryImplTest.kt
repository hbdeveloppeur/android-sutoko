package com.purpletear.game.data.repository.testing

import com.google.gson.Gson
import com.purpletear.game.data.file.testing.TestAssetCacheManager
import com.purpletear.game.data.file.testing.TestPackageExtractor
import com.purpletear.game.data.provider.AndroidGamePathProvider
import com.purpletear.game.data.remote.testing.dto.TestPackageManifestDto
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class TestPackageRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val gson = Gson()
    private val extractor = TestPackageExtractor()
    private lateinit var pathProvider: AndroidGamePathProvider

    @Before
    fun setup() {
        pathProvider = FakeAndroidGamePathProvider(tempFolder.root)
    }

    @Test
    fun `downloadPackage downloads zip extracts it and returns extract directory`() = runBlocking {
        val manifest = TestPackageManifestDto(
            seed = 27,
            chapterId = "chapter-1",
            storyId = "story-1",
            updatedAt = "2024-01-01T00:00:00Z",
            nodes = emptyList(),
            edges = emptyList(),
            assetInventory = emptyList()
        )
        val zipBytes = createZip(
            "manifest.json" to gson.toJson(manifest).toByteArray(),
            "assets/image.png" to "fake-image".toByteArray()
        )
        val client = fakeOkHttpClient(zipBytes)
        val repository = createRepository(client)

        val result = repository.downloadPackage(
            packageUrl = "/test-package/chapter-1/27.zip",
            gameId = "game1",
            chapterId = "chapter-1",
            seed = 27
        )

        assertTrue(result.isSuccess)
        val extractDir = File(result.getOrThrow())
        assertTrue(File(extractDir, "manifest.json").exists())
        assertTrue(File(extractDir, "assets/image.png").exists())

        val gameDir = pathProvider.getGameDirectory("game1", null)
        val tempRoot = File(gameDir, "test-session/.tmp/chapter-1/27")
        val tempChildren = tempRoot.listFiles()
        assertTrue(
            "Temp directory should be cleaned up",
            tempChildren == null || tempChildren.isEmpty()
        )
    }

    @Test
    fun `downloadPackage fails when game directory cannot be created`() = runBlocking {
        val pathProvider = NonWritablePathProvider(tempFolder.root)
        val repository = TestPackageRepositoryImpl(
            client = fakeOkHttpClient(byteArrayOf()),
            baseUrl = "https://canvas.sutoko.com/api/",
            pathProvider = pathProvider,
            extractor = extractor,
            assetCacheManager = TestAssetCacheManager(pathProvider)
        )

        val result = repository.downloadPackage(
            packageUrl = "/test-package/chapter-1/1.zip",
            gameId = "game1",
            chapterId = "chapter-1",
            seed = 1
        )

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(
            "Expected directory creation failure but got $error",
            error is IllegalArgumentException && error.message?.contains("Failed to create game directory") == true
        )
    }

    @Test
    fun `downloadPackage cleans up temp directory on HTTP error`() = runBlocking {
        val client = fakeOkHttpClient(byteArrayOf(), code = 404)
        val repository = createRepository(client)

        val result = repository.downloadPackage(
            packageUrl = "/test-package/chapter-1/1.zip",
            gameId = "game1",
            chapterId = "chapter-1",
            seed = 1
        )

        assertTrue(result.isFailure)
        val gameDir = pathProvider.getGameDirectory("game1", null)
        val tempRoot = File(gameDir, "test-session/.tmp/chapter-1/1")
        val tempChildren = tempRoot.listFiles()
        assertTrue(
            "Temp directory should be cleaned up after failure",
            tempChildren == null || tempChildren.isEmpty()
        )
    }

    @Test
    fun `downloadPackage uses absolute URL unchanged`() = runBlocking {
        val capturedUrls = mutableListOf<String>()
        val client = OkHttpClient.Builder()
            .addInterceptor(RecordingInterceptor(capturedUrls, byteArrayOf(), 200))
            .build()
        val repository = createRepository(client)

        val absoluteUrl = "https://example.com/api/package.zip"
        repository.downloadPackage(
            packageUrl = absoluteUrl,
            gameId = "game1",
            chapterId = "chapter-1",
            seed = 1
        )

        assertEquals(absoluteUrl, capturedUrls.first())
    }

    @Test
    fun `downloadPackage resolves relative URL against base URL`() = runBlocking {
        val capturedUrls = mutableListOf<String>()
        val client = OkHttpClient.Builder()
            .addInterceptor(RecordingInterceptor(capturedUrls, byteArrayOf(), 200))
            .build()
        val repository = createRepository(client)

        repository.downloadPackage(
            packageUrl = "/test-package/chapter-1/1.zip",
            gameId = "game1",
            chapterId = "chapter-1",
            seed = 1
        )

        assertEquals(
            "https://canvas.sutoko.com/api/test-package/chapter-1/1.zip",
            capturedUrls.first()
        )
    }

    private fun createRepository(client: OkHttpClient): TestPackageRepositoryImpl {
        return TestPackageRepositoryImpl(
            client = client,
            baseUrl = "https://canvas.sutoko.com/api/",
            pathProvider = pathProvider,
            extractor = extractor,
            assetCacheManager = TestAssetCacheManager(pathProvider)
        )
    }

    private fun fakeOkHttpClient(body: ByteArray, code: Int = 200): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(FakeInterceptor(body, code))
            .build()
    }

    private fun createZip(vararg entries: Pair<String, ByteArray>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            entries.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }

    private class FakeInterceptor(
        private val body: ByteArray,
        private val code: Int
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(if (code == 200) "OK" else "Error")
                .body(body.toResponseBody("application/zip".toMediaType()))
                .build()
        }
    }

    private class RecordingInterceptor(
        private val capturedUrls: MutableList<String>,
        private val body: ByteArray,
        private val code: Int
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            capturedUrls.add(chain.request().url.toString())
            return Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("OK")
                .body(body.toResponseBody("application/zip".toMediaType()))
                .build()
        }
    }

    private class FakeAndroidGamePathProvider(private val rootDir: File) : AndroidGamePathProvider {
        override fun getStoriesDirectoryPath(): String = File(rootDir, "games").absolutePath
        override fun getStoryDirectoryPath(storyId: String, legacyId: Int?): String =
            File(getGamesDirectory(), legacyId?.toString() ?: storyId).absolutePath
        override fun getGamesDirectory(): File = File(rootDir, "games")
        override fun getGameDirectory(gameId: String, legacyId: Int?): File =
            File(getGamesDirectory(), legacyId?.toString() ?: gameId)
    }

    private class NonWritablePathProvider(private val rootDir: File) : AndroidGamePathProvider {
        override fun getStoriesDirectoryPath(): String = File(rootDir, "games").absolutePath
        override fun getStoryDirectoryPath(storyId: String, legacyId: Int?): String =
            File(getGamesDirectory(), legacyId?.toString() ?: storyId).absolutePath
        override fun getGamesDirectory(): File = File(rootDir, "games")
        override fun getGameDirectory(gameId: String, legacyId: Int?): File {
            // Create the parent as a regular file so mkdirs() on the game directory fails.
            getGamesDirectory().createNewFile()
            return File(getGamesDirectory(), legacyId?.toString() ?: gameId)
        }
    }
}
