package com.purpletear.game.data.file

import com.purpletear.game.data.provider.AndroidGamePathProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

class GameFileManagerImpl @Inject constructor(
    private val pathProvider: AndroidGamePathProvider
) : GameFileManager {

    private val baseDir: File by lazy {
        pathProvider.getGamesDirectory().also { it.mkdirs() }
    }

    private fun getGameDir(gameId: String): File {
        require(gameId.isNotBlank()) { "gameId must not be blank" }
        require(gameId.none { it == '/' || it == '\\' }) {
            "gameId must not contain path separators: $gameId"
        }
        val dir = File(baseDir, gameId)
        val canonicalDir = dir.canonicalFile
        val canonicalBase = baseDir.canonicalFile
        if (!canonicalDir.path.startsWith(canonicalBase.path + File.separator) &&
            canonicalDir != canonicalBase
        ) {
            throw SecurityException("gameId escapes base directory: $gameId")
        }
        return dir
    }

    override suspend fun downloadAndExtract(
        gameId: String,
        downloadUrl: String,
        onProgress: suspend (Float) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val gameDir = getGameDir(gameId)
        val tempDir = File(baseDir, "$gameId.tmp")
        val extractDir = File(tempDir, EXTRACTED_DIR)

        try {
            tempDir.deleteRecursively()
            tempDir.mkdirs()
            extractDir.mkdirs()

            val archiveFile = File(tempDir, ARCHIVE_NAME)

            val connection =
                java.net.URL(downloadUrl).openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 30_000
            connection.readTimeout = 30_000
            connection.requestMethod = "GET"
            connection.instanceFollowRedirects = true

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                throw IOException("Download failed. HTTP $responseCode. Body: $errorBody")
            }

            val totalBytes = connection.contentLengthLong.takeIf { it > 0 } ?: 1L

            connection.inputStream.use { input ->
                archiveFile.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var copied = 0L

                    while (coroutineContext.isActive) {
                        val bytes = input.read(buffer)
                        if (bytes < 0) break

                        output.write(buffer, 0, bytes)
                        copied += bytes

                        val progress = if (connection.contentLengthLong > 0) {
                            copied.toFloat() / totalBytes.toFloat()
                        } else {
                            0f
                        }

                        onProgress(progress.coerceIn(0f, 0.99f))
                    }
                }
            }

            connection.disconnect()

            if (!coroutineContext.isActive) {
                throw CancellationException("Download cancelled")
            }

            extractZip(archiveFile, extractDir)

            if (!coroutineContext.isActive) {
                throw CancellationException("Extraction cancelled")
            }

            archiveFile.delete()
            gameDir.deleteRecursively()
            if (!extractDir.renameTo(gameDir)) {
                throw IOException("Failed to move extracted game to final directory")
            }

            val expectedIndex = File(gameDir, SCENES_INDEX)
            if (!expectedIndex.exists()) {
                throw IOException(
                    "Downloaded game is missing expected index file: ${expectedIndex.absolutePath}"
                )
            }

            gameDir.absolutePath
        } catch (e: Throwable) {
            tempDir.deleteRecursively()
            throw e
        } finally {
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
            }
        }
    }

    override fun getInstallPath(gameId: String): String =
        getGameDir(gameId).absolutePath

    override suspend fun deleteGame(gameId: String) {
        withContext(Dispatchers.IO) {
            getGameDir(gameId).deleteRecursively()
        }
    }

    private suspend fun extractZip(archiveFile: File, extractDir: File) {
        archiveFile.inputStream().use { fis ->
            ZipInputStream(fis).use { zis ->
                var entry = zis.nextEntry
                while (entry != null && coroutineContext.isActive) {
                    val entryFile = safeEntryFile(extractDir, entry.name)
                    if (entry.isDirectory) {
                        entryFile.mkdirs()
                    } else {
                        entryFile.parentFile?.mkdirs()
                        entryFile.outputStream().use { entryOutput ->
                            copyWithCancellation(zis, entryOutput)
                        }
                    }
                    entry = zis.nextEntry
                }
            }
        }
    }

    private fun safeEntryFile(extractDir: File, entryName: String): File {
        val normalized = entryName.replace("\\", "/")
        if (normalized.contains("..")) {
            throw SecurityException("ZIP entry contains path traversal: $entryName")
        }
        val entryFile = File(extractDir, normalized).canonicalFile
        val canonicalExtractDir = extractDir.canonicalFile
        if (!entryFile.path.startsWith(canonicalExtractDir.path + File.separator) &&
            entryFile != canonicalExtractDir
        ) {
            throw SecurityException("ZIP entry escapes extraction directory: $entryName")
        }
        return entryFile
    }

    private suspend fun copyWithCancellation(source: InputStream, output: OutputStream) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var bytes = source.read(buffer)
        while (bytes >= 0 && coroutineContext.isActive) {
            output.write(buffer, 0, bytes)
            bytes = source.read(buffer)
        }
    }

    companion object {
        private const val ARCHIVE_NAME = "archive.zip"
        private const val EXTRACTED_DIR = "extracted"
        private const val SCENES_INDEX = "scenes/scenes.json"
    }
}
