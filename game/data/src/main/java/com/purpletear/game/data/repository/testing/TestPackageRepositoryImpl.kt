package com.purpletear.game.data.repository.testing

import com.google.gson.Gson
import com.purpletear.game.data.di.TestingBaseUrl
import com.purpletear.game.data.di.TestingOkHttpClient
import com.purpletear.game.data.file.testing.TestAssetCacheManager
import com.purpletear.game.data.file.testing.TestPackageExtractor
import com.purpletear.game.data.provider.AndroidGamePathProvider
import com.purpletear.game.data.remote.testing.dto.TestPackageManifestDto
import com.purpletear.game.data.remote.testing.dto.parseManifest
import com.purpletear.sutoko.game.model.testing.TestPackageManifest
import com.purpletear.sutoko.game.repository.testing.TestPackageRepository
import com.purpletear.sutoko.game.testing.StoryTestingLogger

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
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
            val tempDir = File(gameDir, "test-session/.tmp/$chapterId/$seed")
            tempDir.mkdirs()

            val archiveFile = File(tempDir, "package.zip")
            val resolvedUrl = resolvePackageUrl(packageUrl)
            downloadToFile(resolvedUrl, archiveFile)

            val extractDir = File(gameDir, "test-session/$chapterId/$seed")
            extractDir.mkdirs()
            extractor.extract(archiveFile, extractDir)

            archiveFile.delete()
            tempDir.deleteRecursively()

            StoryTestingLogger.d("PKG") { "Package extracted — $extractDir" }
            extractDir.absolutePath
        }.onFailure { error ->
            StoryTestingLogger.e("PKG", error) { "Package download/extract failed — $chapterId seed $seed" }
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
                assetInventory = dto.assetInventory,
                extractedDirectory = extractedDirectory,
            )
        }.onFailure { error ->
            StoryTestingLogger.e("PKG", error) { "Apply package failed — $extractedDirectory" }
        }
    }

    private suspend fun downloadToFile(url: String, destination: File) =
        withContext(Dispatchers.IO) {
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
                        val bytes = input.copyTo(output)
                        StoryTestingLogger.d("PKG") { "Downloaded $bytes bytes (declared=$contentLength)" }
                    }
                }
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
