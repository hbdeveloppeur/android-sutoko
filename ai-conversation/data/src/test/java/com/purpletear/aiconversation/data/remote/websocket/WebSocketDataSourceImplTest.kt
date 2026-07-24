package com.purpletear.aiconversation.data.remote.websocket

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.purpletear.aiconversation.data.parser.WebsocketMessageParserImpl
import com.purpletear.aiconversation.domain.enums.MessageRole
import com.purpletear.aiconversation.domain.model.messages.entities.MessageText
import com.purpletear.aiconversation.domain.sealed.WebSocketMessage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Reproduces the exact frame sequence of the Arashai backend
 * (api/src/Websocket/WebSocket.php):
 * - server pushes {"action":"connected"} right after the handshake,
 * - the client must answer with an "authenticate" frame,
 * - server then answers {"action":"authenticated"}.
 */
class WebSocketDataSourceImplTest {

    private lateinit var server: MockWebServer
    private val connectionClosedLatches = mutableListOf<CountDownLatch>()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        // Let the client's awaitClose complete the close handshake first,
        // otherwise MockWebServer.shutdown() fails on lingering sockets.
        connectionClosedLatches.forEach { it.await(5, TimeUnit.SECONDS) }
        server.shutdown()
    }

    private fun newDataSource(): WebSocketDataSourceImpl {
        val request = Request.Builder().url(server.url("/connect/ws")).build()
        return WebSocketDataSourceImpl(request, WebsocketMessageParserImpl(Gson()))
    }

    private inner class BackendListener(
        private val framesOnOpen: List<String> = listOf("""{"action":"connected"}"""),
        private val framesAfterAuth: List<String> = emptyList(),
        private val closeAfterAuth: Boolean = false,
        private val respondToAuth: Boolean = true,
        private val onAuthenticateFrame: (String) -> Unit = {},
    ) : WebSocketListener() {
        private val closed = CountDownLatch(1).also { connectionClosedLatches.add(it) }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            framesOnOpen.forEach { webSocket.send(it) }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val action = JsonParser.parseString(text).asJsonObject["action"].asString
            if (action == "authenticate") {
                onAuthenticateFrame(text)
                if (!respondToAuth) return
                webSocket.send("""{"action":"authenticated"}""")
                framesAfterAuth.forEach { webSocket.send(it) }
                if (closeAfterAuth) {
                    webSocket.close(1000, "server shutdown")
                }
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(code, reason)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            closed.countDown()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            closed.countDown()
        }
    }

    private fun enqueueBackend(listener: BackendListener) {
        server.enqueue(MockResponse().withWebSocketUpgrade(listener))
    }

    @Test
    fun `connected frame emits nothing, authenticated emits AuthenticateSuccess`() = runBlocking {
        val authFrame = CompletableDeferred<String>()
        enqueueBackend(BackendListener(onAuthenticateFrame = { authFrame.complete(it) }))
        val dataSource = newDataSource()

        val received = withTimeout(15_000) {
            dataSource.connect("uid-1", "token-1").take(1).toList()
        }

        assertEquals(listOf(WebSocketMessage.AuthenticateSuccess), received)
        assertTrue(dataSource.isConnected)

        val auth = JsonParser.parseString(authFrame.await()).asJsonObject
        assertEquals("authenticate", auth["action"].asString)
        assertEquals("uid-1", auth["uid"].asString)
        assertEquals("token-1", auth["token"].asString)
    }

    @Test
    fun `new_message frame is parsed to a MessageText`() = runBlocking {
        // Exact shape produced by the backend's ClientNotificationMessage::toJson()
        val newMessage = """
            {"action":"new_message","id":"msg-42","text":"Hello from Eva",
             "aiCharacterId":"7","role":"assistant","type":"text",
             "conversationId":"9","userId":"uid-1","os":"game"}
        """.trimIndent()
        enqueueBackend(BackendListener(framesAfterAuth = listOf(newMessage)))
        val dataSource = newDataSource()

        val received = withTimeout(15_000) {
            dataSource.connect("uid-1", "token-1").take(2).toList()
        }

        assertEquals(WebSocketMessage.AuthenticateSuccess, received[0])
        val chatMessage = received[1] as WebSocketMessage.ChatMessage
        val message = chatMessage.message as MessageText
        assertEquals("msg-42", message.id)
        assertEquals("Hello from Eva", message.text)
        assertEquals(MessageRole.Assistant, message.role)
        assertEquals(7, message.aiCharacterId)
    }

    @Test
    fun `reconnect after flow closure still works`() = runBlocking {
        // Regression test: awaitClose must not kill the shared OkHttpClient,
        // otherwise every reconnection fails until process death.
        enqueueBackend(BackendListener())
        enqueueBackend(BackendListener())
        val dataSource = newDataSource()

        val first = withTimeout(15_000) {
            dataSource.connect("uid-1", "token-1").take(1).toList()
        }
        assertEquals(listOf(WebSocketMessage.AuthenticateSuccess), first)

        val second = withTimeout(15_000) {
            dataSource.connect("uid-1", "token-1").take(1).toList()
        }
        assertEquals(listOf(WebSocketMessage.AuthenticateSuccess), second)
    }

    @Test
    fun `server-initiated close emits Error and reconnection still works`() = runBlocking {
        // Regression test: a normal close used to complete the flow silently,
        // killing live updates until the user left and re-entered the screen.
        enqueueBackend(BackendListener(closeAfterAuth = true))
        enqueueBackend(BackendListener())
        val dataSource = newDataSource()

        val received = withTimeout(15_000) {
            dataSource.connect("uid-1", "token-1").take(2).toList()
        }
        assertEquals(listOf(WebSocketMessage.AuthenticateSuccess, WebSocketMessage.Error), received)
        assertFalse(dataSource.isConnected)

        val second = withTimeout(15_000) {
            dataSource.connect("uid-1", "token-1").take(1).toList()
        }
        assertEquals(listOf(WebSocketMessage.AuthenticateSuccess), second)
        assertTrue(dataSource.isConnected)
    }

    @Test
    fun `unknown action frame is ignored and flow stays alive`() = runBlocking {
        val listener = BackendListener(
            framesOnOpen = listOf(
                """{"action":"some_future_action","foo":1}""",
                """{"action":"connected"}""",
            )
        )
        server.enqueue(MockResponse().withWebSocketUpgrade(listener))
        val dataSource = newDataSource()

        val received = withTimeout(15_000) {
            dataSource.connect("uid-1", "token-1").take(1).toList()
        }

        assertEquals(listOf(WebSocketMessage.AuthenticateSuccess), received)
    }

    @Test
    fun `server that never authenticates emits Error within the auth timeout`() = runBlocking {
        // Regression test: a server that accepts the upgrade but never sends
        // "authenticated" used to leave the conversation screen loading forever.
        enqueueBackend(BackendListener(respondToAuth = false))
        val request = Request.Builder().url(server.url("/connect/ws")).build()
        val dataSource = WebSocketDataSourceImpl(
            request,
            WebsocketMessageParserImpl(Gson()),
            authTimeoutMs = 1_000L,
        )

        val received = withTimeout(15_000) {
            dataSource.connect("uid-1", "token-1").take(1).toList()
        }

        assertEquals(listOf(WebSocketMessage.Error), received)
        assertFalse(dataSource.isConnected)
    }
}
