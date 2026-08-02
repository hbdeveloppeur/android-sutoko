package com.purpletear.game.presentation.model

import com.purpletear.sutoko.game.exception.GameDownloadForbiddenException
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class GameUiErrorTest {

    @Test
    fun `forbidden exception maps to DownloadForbidden`() {
        assertEquals(
            GameUiError.DownloadForbidden,
            GameUiError.fromDownloadError(GameDownloadForbiddenException("163"))
        )
    }

    @Test
    fun `network failures map to Download`() {
        assertEquals(GameUiError.Download, GameUiError.fromDownloadError(UnknownHostException()))
        assertEquals(GameUiError.Download, GameUiError.fromDownloadError(SocketTimeoutException()))
        assertEquals(GameUiError.Download, GameUiError.fromDownloadError(ConnectException()))
        assertEquals(GameUiError.Download, GameUiError.fromDownloadError(SSLException("handshake")))
    }

    @Test
    fun `network failure wrapped in another exception maps to Download`() {
        val wrapped = IOException("Download failed", UnknownHostException("sutoko.com"))
        assertEquals(GameUiError.Download, GameUiError.fromDownloadError(wrapped))
    }

    @Test
    fun `other failures map to DownloadUnknown`() {
        assertEquals(GameUiError.DownloadUnknown, GameUiError.fromDownloadError(IOException("HTTP 500")))
        assertEquals(GameUiError.DownloadUnknown, GameUiError.fromDownloadError(IllegalStateException("corrupt zip")))
    }
}
