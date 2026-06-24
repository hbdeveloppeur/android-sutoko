package com.purpletear.game.data.file

import com.purpletear.game.data.provider.AndroidGamePathProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.BufferedReader
import java.io.File
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
            scenesFile.relativeTo(expectedGameDir).path,
            "{}"
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

        val archiveBytes = createZipArchiveBytes("../evil.json", "{}")
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

    private fun startServer(body: ByteArray): String {
        val server = ServerSocket(0)
        val port = server.localPort

        thread(isDaemon = true) {
            server.use { listener ->
                listener.accept().use { socket ->
                    serve(socket, body)
                }
            }
        }

        return "http://127.0.0.1:$port/game.zip"
    }

    private fun serve(socket: Socket, body: ByteArray) {
        socket.getInputStream().bufferedReader().use { reader ->
            socket.getOutputStream().use { output ->
                readRequest(reader)

                val writer = PrintWriter(output.bufferedWriter(), true)
                writer.println("HTTP/1.1 200 OK")
                writer.println("Content-Type: application/zip")
                writer.println("Content-Length: ${body.size}")
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

    private fun createZipArchiveBytes(entryName: String, content: String): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        ZipOutputStream(output).use { zos ->
            val entry = ZipEntry(entryName)
            zos.putNextEntry(entry)
            zos.write(content.toByteArray())
            zos.closeEntry()
        }
        return output.toByteArray()
    }

    private class FakeAndroidGamePathProvider(private val gamesDir: File) : AndroidGamePathProvider {
        override fun getStoriesDirectoryPath(): String = gamesDir.absolutePath
        override fun getStoryDirectoryPath(storyId: String): String =
            File(gamesDir, storyId).absolutePath

        override fun getGamesDirectory(): File = gamesDir
    }
}
