package com.purpletear.game.data.repository.testing

import com.google.gson.Gson
import com.purpletear.game.data.di.TestingBaseUrl
import com.purpletear.game.data.di.TestingOkHttpClient
import com.purpletear.game.data.file.testing.TestAssetCacheManager
import com.purpletear.game.data.file.testing.TestPackageExtractor
import com.purpletear.game.data.provider.AndroidGamePathProvider
import com.purpletear.game.data.remote.testing.dto.parseManifest
import com.purpletear.sutoko.game.model.testing.TestPackageManifest
import com.purpletear.sutoko.game.repository.testing.TestPackageRepository
import com.purpletear.sutoko.game.testing.StoryTestingLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestPackageRepositoryImpl @Inject constructor(
    @TestingOkHttpClient private val client: OkHttpClient,
    @TestingBaseUrl private val baseUrl: String,
    private val pathProvider: AndroidGamePathProvider,
    private val extractor: TestPackageExtractor,
    private val assetCacheManager: TestAssetCacheManager,
) : TestPackageRepository {

    private val gson = Gson()

    override suspend fun downloadPackage(
        packageUrl: String,
        gameId: String,
        chapterId: String,
        seed: Int,
    ): Result<String> = withContext(Dispatchers.IO) {
        StoryTestingLogger.d("PKG") { "Download package — $chapterId seed $seed from $packageUrl" }
        runCatching {
            val gameDir = pathProvider.getGameDirectory(gameId = gameId, legacyId = null)
            require(gameDir.exists() || gameDir.mkdirs()) {
                "Failed to create game directory: $gameDir"
            }

            val extractDir = File(gameDir, "test-session/$chapterId/$seed")
            if (isExtractedPackageValid(extractDir, chapterId, seed)) {
                StoryTestingLogger.i("PKG") { "Package cache hit — $chapterId seed $seed" }
                return@runCatching extractDir.absolutePath
            }

            val downloadId = System.currentTimeMillis().toString()
            val tempDir = File(gameDir, "test-session/.tmp/$chapterId/$seed/$downloadId")
            require(tempDir.mkdirs()) {
                "Failed to create temp directory: $tempDir"
            }

            val archiveFile = File(tempDir, "package.zip")
            val tempExtractDir = File(gameDir, "test-session/$chapterId/$seed.tmp")
            val resolvedUrl = resolvePackageUrl(packageUrl)

            try {
                downloadToFile(resolvedUrl, archiveFile)

                tempExtractDir.deleteRecursively()
                extractor.extract(archiveFile, tempExtractDir)

                extractDir.deleteRecursively()
                check(tempExtractDir.renameTo(extractDir)) {
                    "Failed to move extracted package to $extractDir"
                }

                StoryTestingLogger.d("PKG") { "Package extracted — $extractDir" }
                extractDir.absolutePath
            } finally {
                archiveFile.delete()
                tempDir.deleteRecursively()
                tempExtractDir.deleteRecursively()
            }
        }.onFailure { error ->
            StoryTestingLogger.e(
                "PKG",
                error
            ) { "Package download/extract failed — $chapterId seed $seed" }
        }
    }

    override suspend fun applyPackage(
        extractedDirectory: String,
        gameId: String,
    ): Result<TestPackageManifest> = withContext(Dispatchers.IO) {
        StoryTestingLogger.d("PKG") { "Applying package — $extractedDirectory" }
        runCatching {
            val extractDir = File(extractedDirectory)
            val manifestFile = File(extractDir, MANIFEST_FILE)
            require(manifestFile.exists()) { "Manifest not found in $extractedDirectory" }

            val dto = gson.parseManifest(manifestFile.readText())
            val assetsDir = File(extractDir, ASSETS_DIR)
            val copied = assetCacheManager.copyAssets(assetsDir, gameId)

            StoryTestingLogger.i("PKG") { "Package applied — ${dto.chapterId} seed ${dto.seed}, ${copied.size} assets copied" }
            TestPackageManifest(
                seed = dto.seed,
                chapterId = dto.chapterId,
                storyId = dto.storyId,
                updatedAt = dto.updatedAt,
                assetInventory = dto.assetInventory.map { it.uniqueFileName },
                extractedDirectory = extractedDirectory,
            )
        }.onFailure { error ->
            StoryTestingLogger.e(
                "PKG",
                error
            ) { "Apply package failed — $extractedDirectory (error: ${error.message}" }
        }
    }

    private suspend fun downloadToFile(url: String, destination: File) {
        val request = Request.Builder()
            .url(url)
            .header("Cache-Control", "no-cache")
            .build()

        StoryTestingLogger.d("PKG") { "Downloading $url" }
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw TestPackageException("Download failed: HTTP ${response.code}")
            }
            val body = response.body ?: throw TestPackageException("Empty response body")
            val contentLength = body.contentLength()
            body.byteStream().use { input ->
                destination.outputStream().use { output ->
                    val bytes = copyCancellable(input, output)
                    StoryTestingLogger.d("PKG") { "Downloaded $bytes bytes (declared=$contentLength)" }
                }
            }
        }
    }

    private suspend fun copyCancellable(input: InputStream, output: OutputStream): Long {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            currentCoroutineContext().ensureActive()
            val read = input.read(buffer)
            if (read == -1) break
            output.write(buffer, 0, read)
            total += read
        }
        return total
    }

    private fun isExtractedPackageValid(extractDir: File, chapterId: String, seed: Int): Boolean {
        val manifestFile = File(extractDir, MANIFEST_FILE)
        if (!manifestFile.exists()) return false

        return try {
            val dto = gson.parseManifest(manifestFile.readText())
            dto.chapterId == chapterId && dto.seed == seed
        } catch (e: Exception) {
            StoryTestingLogger.d("PKG") { "Cached manifest invalid — ${e.message}" }
            false
        }
    }

    private fun resolvePackageUrl(packageUrl: String): String {
        return if (packageUrl.startsWith("http://") || packageUrl.startsWith("https://")) {
            packageUrl
        } else {
            "${baseUrl}$packageUrl"
        }.replace("//", "/").replace(":/", "://")
    }

    private companion object {
        const val MANIFEST_FILE = "manifest.json"
        const val ASSETS_DIR = "assets"
    }
}

class TestPackageException(message: String) : Exception(message)
