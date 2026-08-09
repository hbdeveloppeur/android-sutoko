package com.purpletear.game.data.file

import com.purpletear.game.data.provider.AndroidGamePathProvider
import com.purpletear.sutoko.game.exception.GameDownloadForbiddenException
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

    private fun getGameDir(gameId: String, legacyId: Int?): File {
        require(gameId.isNotBlank()) { "gameId must not be blank" }
        val dirName = directoryName(gameId, legacyId)
        require(dirName.none { it == '/' || it == '\\' }) {
            "game directory name must not contain path separators: $dirName"
        }
        val dir = File(baseDir, dirName)
        val canonicalDir = dir.canonicalFile
        val canonicalBase = baseDir.canonicalFile
        if (!canonicalDir.path.startsWith(canonicalBase.path + File.separator) &&
            canonicalDir != canonicalBase
        ) {
            throw SecurityException("game directory escapes base directory: $dirName")
        }
        return dir
    }

    override suspend fun downloadAndExtract(
        gameId: String,
        downloadUrl: String,
        onProgress: suspend (Float) -> Unit,
        legacyId: Int?,
    ): String = withContext(Dispatchers.IO) {
        val gameDir = getGameDir(gameId, legacyId)
        val dirName = directoryName(gameId, legacyId)
        val tempDir = File(baseDir, "$dirName.tmp")
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
                if (responseCode == java.net.HttpURLConnection.HTTP_FORBIDDEN) {
                    throw GameDownloadForbiddenException()
                }
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                throw IOException("Download failed. HTTP $responseCode. Body: $errorBody")
            }

            val expectedBytes = connection.contentLengthLong
            val totalBytes = expectedBytes.takeIf { it > 0 } ?: 1L

            var copied = 0L
            connection.inputStream.use { input ->
                archiveFile.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

                    while (coroutineContext.isActive) {
                        val bytes = input.read(buffer)
                        if (bytes < 0) break

                        output.write(buffer, 0, bytes)
                        copied += bytes

                        val progress = if (expectedBytes > 0) {
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

            if (expectedBytes > 0 && copied != expectedBytes) {
                throw IOException("Incomplete download for game $gameId: $copied/$expectedBytes bytes")
            }

            extractZip(archiveFile, extractDir)

            if (!coroutineContext.isActive) {
                throw CancellationException("Extraction cancelled")
            }

            // A playable game must ship at least one chapter language; anything else is a
            // corrupt or truncated archive and must not be marked as installed.
            val chaptersDir = File(extractDir, CHAPTERS_DIR)
            if (chaptersDir.listFiles()?.any { it.isDirectory } != true) {
                throw IOException("Invalid archive for game $gameId: no chapter languages found")
            }

            archiveFile.delete()
            gameDir.deleteRecursively()
            if (!extractDir.renameTo(gameDir)) {
                throw IOException("Failed to move extracted game to final directory")
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

    override fun getInstallPath(gameId: String, legacyId: Int?): String =
        getGameDir(gameId, legacyId).absolutePath

    override suspend fun deleteGame(gameId: String, legacyId: Int?) {
        withContext(Dispatchers.IO) {
            getGameDir(gameId, legacyId).deleteRecursively()
        }
    }

    private fun directoryName(gameId: String, legacyId: Int?): String {
        return legacyId?.toString() ?: gameId
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
        private const val CHAPTERS_DIR = "chapters"
    }
}
