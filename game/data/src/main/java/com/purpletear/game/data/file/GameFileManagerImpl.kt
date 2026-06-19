package com.purpletear.game.data.file

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context
) : GameFileManager {

    private val baseDir: File by lazy {
        val externalDir = context.getExternalFilesDir(null)
            ?: throw IOException("External files directory not available")
        File(externalDir, GAMES_DIR).also { it.mkdirs() }
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

        try {
            tempDir.deleteRecursively()
            tempDir.mkdirs()

            val archiveFile = File(tempDir, ARCHIVE_NAME)
            val extractDir = File(tempDir, EXTRACTED_DIR)

            // Copy from assets with progress
            val totalBytes = context.assets.openFd(downloadUrl).length.coerceAtLeast(1L)
            context.assets.open(downloadUrl).use { input ->
                archiveFile.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var copied = 0L
                    var bytes = input.read(buffer)
                    while (bytes >= 0 && coroutineContext.isActive) {
                        output.write(buffer, 0, bytes)
                        copied += bytes
                        onProgress((copied.toFloat() / totalBytes.toFloat()).coerceIn(0f, 0.99f))
                        bytes = input.read(buffer)
                    }
                }
            }

            if (!coroutineContext.isActive) {
                throw CancellationException("Download cancelled")
            }

            // Extract ZIP
            extractDir.mkdirs()
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

            if (!coroutineContext.isActive) {
                throw CancellationException("Extraction cancelled")
            }

            // Atomically replace old install with new one
            archiveFile.delete()
            gameDir.deleteRecursively()
            if (!tempDir.renameTo(gameDir)) {
                throw IOException("Failed to move extracted game to final directory")
            }

            File(gameDir, EXTRACTED_DIR).absolutePath
        } catch (e: Throwable) {
            tempDir.deleteRecursively()
            throw e
        }
    }

    override fun getInstallPath(gameId: String): String =
        getGameDir(gameId).absolutePath

    override suspend fun deleteGame(gameId: String) {
        withContext(Dispatchers.IO) {
            getGameDir(gameId).deleteRecursively()
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
        private const val GAMES_DIR = "games"
        private const val ARCHIVE_NAME = "archive.zip"
        private const val EXTRACTED_DIR = "extracted"
    }
}
