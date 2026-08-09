package com.purpletear.game.data.file

import com.purpletear.game.data.provider.AndroidGamePathProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.concurrent.thread

class GameFileManagerImplTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `downloadAndExtract writes game files directly under provider game directory`() = runTest {
        val gamesDir = temporaryFolder.newFolder("games")
        val pathProvider = FakeAndroidGamePathProvider(gamesDir)
        val fileManager = GameFileManagerImpl(pathProvider)
        val gameId = "friendzone1"

        val expectedGameDir = File(gamesDir, gameId)
        val scenesFile = File(expectedGameDir, "scenes/scenes.json")
        val archiveBytes = createZipArchiveBytes(
            scenesFile.relativeTo(expectedGameDir).path to "{}",
            "chapters/en/1a/nodes.json" to "[]"
        )

        val downloadUrl = startServer(archiveBytes)
        val progressValues = mutableListOf<Float>()

        val returnedPath = fileManager.downloadAndExtract(
            gameId = gameId,
            downloadUrl = downloadUrl,
            onProgress = { progressValues.add(it) }
        )

        assertEquals(expectedGameDir.absolutePath, returnedPath)
        assertTrue(
            "Expected scenes index at ${scenesFile.absolutePath}",
            scenesFile.exists()
        )
        assertTrue(progressValues.isNotEmpty())
        assertTrue(progressValues.all { it in 0f..0.99f })
    }

    @Test
    fun `downloadAndExtract rejects zip entries that escape the game directory`() = runTest {
        val gamesDir = temporaryFolder.newFolder("games")
        val pathProvider = FakeAndroidGamePathProvider(gamesDir)
        val fileManager = GameFileManagerImpl(pathProvider)

        val archiveBytes = createZipArchiveBytes("../evil.json" to "{}")
        val downloadUrl = startServer(archiveBytes)

        var threw = false
        try {
            fileManager.downloadAndExtract(
                gameId = "evil",
                downloadUrl = downloadUrl,
                onProgress = {}
            )
        } catch (e: SecurityException) {
            threw = true
        }

        assertTrue("Expected SecurityException for path-traversing ZIP entry", threw)
    }

    @Test
    fun `downloadAndExtract rejects archive without chapter languages`() = runTest {
        val gamesDir = temporaryFolder.newFolder("games")
        val pathProvider = FakeAndroidGamePathProvider(gamesDir)
        val fileManager = GameFileManagerImpl(pathProvider)
        val gameId = "nochapters"

        val archiveBytes = createZipArchiveBytes("scenes/scenes.json" to "{}")
        val downloadUrl = startServer(archiveBytes)

        var threw = false
        try {
            fileManager.downloadAndExtract(
                gameId = gameId,
                downloadUrl = downloadUrl,
                onProgress = {}
            )
        } catch (e: IOException) {
            threw = true
        }

        assertTrue("Expected IOException for archive without chapter languages", threw)
        assertFalse(
            "Broken archive must not be moved into the game directory",
            File(gamesDir, gameId).exists()
        )
    }

    @Test
    fun `downloadAndExtract rejects truncated download`() = runTest {
        val gamesDir = temporaryFolder.newFolder("games")
        val pathProvider = FakeAndroidGamePathProvider(gamesDir)
        val fileManager = GameFileManagerImpl(pathProvider)
        val gameId = "truncated"

        val archiveBytes = createZipArchiveBytes("chapters/en/1a/nodes.json" to "[]")
        val downloadUrl = startServer(archiveBytes, reportedLength = archiveBytes.size + 1024)

        var threw = false
        try {
            fileManager.downloadAndExtract(
                gameId = gameId,
                downloadUrl = downloadUrl,
                onProgress = {}
            )
        } catch (e: IOException) {
            threw = true
        }

        assertTrue("Expected IOException for truncated download", threw)
        assertFalse(
            "Truncated archive must not be moved into the game directory",
            File(gamesDir, gameId).exists()
        )
    }

    private fun startServer(body: ByteArray, reportedLength: Int = body.size): String {
        val server = ServerSocket(0)
        val port = server.localPort

        thread(isDaemon = true) {
            server.use { listener ->
                listener.accept().use { socket ->
                    serve(socket, body, reportedLength)
                }
            }
        }

        return "http://127.0.0.1:$port/game.zip"
    }

    private fun serve(socket: Socket, body: ByteArray, reportedLength: Int) {
        socket.getInputStream().bufferedReader().use { reader ->
            socket.getOutputStream().use { output ->
                readRequest(reader)

                val writer = PrintWriter(output.bufferedWriter(), true)
                writer.println("HTTP/1.1 200 OK")
                writer.println("Content-Type: application/zip")
                writer.println("Content-Length: $reportedLength")
                writer.println("Connection: close")
                writer.println()
                output.write(body)
                output.flush()
            }
        }
    }

    private fun readRequest(reader: BufferedReader) {
        var line: String?
        do {
            line = reader.readLine()
        } while (line != null && line.isNotEmpty())
    }

    private fun createZipArchiveBytes(vararg entries: Pair<String, String>): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        ZipOutputStream(output).use { zos ->
            entries.forEach { (name, content) ->
                val entry = ZipEntry(name)
                zos.putNextEntry(entry)
                zos.write(content.toByteArray())
                zos.closeEntry()
            }
        }
        return output.toByteArray()
    }

    private class FakeAndroidGamePathProvider(private val gamesDir: File) : AndroidGamePathProvider {
        override fun getStoriesDirectoryPath(): String = gamesDir.absolutePath
        override fun getStoryDirectoryPath(storyId: String, legacyId: Int?): String =
            File(gamesDir, legacyId?.toString() ?: storyId).absolutePath

        override fun getGamesDirectory(): File = gamesDir
        override fun getGameDirectory(gameId: String, legacyId: Int?): File =
            File(gamesDir, legacyId?.toString() ?: gameId)
    }
}
