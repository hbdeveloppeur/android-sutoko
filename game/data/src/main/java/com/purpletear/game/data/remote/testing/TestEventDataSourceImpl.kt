package com.purpletear.game.data.remote.testing

import com.purpletear.game.data.di.TestingBaseUrl
import com.purpletear.game.data.di.TestingOkHttpClient
import com.purpletear.game.data.repository.testing.TestSessionException
import com.purpletear.sutoko.domain.repository.UserRepository
import com.purpletear.sutoko.game.model.testing.TestEvent
import com.purpletear.sutoko.game.repository.testing.TestEventDataSource
import com.purpletear.sutoko.game.testing.StoryTestingLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.Volatile

@Singleton
class TestEventDataSourceImpl @Inject constructor(
    @TestingOkHttpClient private val client: OkHttpClient,
    private val userRepository: UserRepository,
    @TestingBaseUrl private val baseUrl: String,
) : TestEventDataSource {

    @Volatile
    private var activeCall: okhttp3.Call? = null
    private var lastEventId: String? = null

    @Volatile
    private var isClosed = false

    override fun events(sessionId: String, inventoryToken: String?): Flow<TestEvent> = callbackFlow {
        isClosed = false
        lastEventId = null
        var consecutiveFailures = 0
        StoryTestingLogger.d("NET") { "SSE stream starting — sessionId=$sessionId" }

        fun onTransportFailure(message: String, cause: Throwable? = null) {
            if (isClosed) {
                StoryTestingLogger.d("NET") { "SSE $message ignored — stream closed by consumer" }
                return
            }
            consecutiveFailures++
            StoryTestingLogger.e("NET", cause) {
                "$message (failure $consecutiveFailures/$MAX_CONSECUTIVE_FAILURES)"
            }
            if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                StoryTestingLogger.e("NET") { "SSE giving up after $consecutiveFailures consecutive failures" }
                trySend(TestEvent.Error("connection_error", "SSE connection failed after $consecutiveFailures attempts"))
                close()
            }
        }

        try {
            while (isActive && !isClosed) {
                val response = try {
                    openConnection(sessionId, inventoryToken)
                } catch (e: TestSessionException) {
                    StoryTestingLogger.e("NET", e) { "SSE terminal setup error" }
                    trySend(TestEvent.Error("setup_error", e.message ?: "SSE setup failed"))
                    close()
                    break
                }

                if (response == null) {
                    onTransportFailure("SSE connection failed")
                    if (!isClosed) {
                        delay(RECONNECT_DELAY_MS)
                    }
                    continue
                }

                consecutiveFailures = 0

                try {
                    readEvents(response)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    onTransportFailure("SSE read error", e)
                } finally {
                    response.closeQuietly()
                }

                if (!isClosed) {
                    StoryTestingLogger.d("NET") { "SSE connection closed, reconnecting in ${RECONNECT_DELAY_MS}ms" }
                    delay(RECONNECT_DELAY_MS)
                }
            }
        } catch (_: CancellationException) {
            StoryTestingLogger.d("NET") { "SSE stream cancelled" }
            // Flow collector cancelled; clean up happens in awaitClose.
        }

        awaitClose { close() }
    }.flowOn(Dispatchers.IO)

    private suspend fun openConnection(
        sessionId: String,
        inventoryToken: String?
    ): Response? {
        val token = userRepository.observeUser().firstOrNull()?.token
        if (token == null) {
            StoryTestingLogger.w("NET") { "No auth token available for SSE" }
            return null
        }

        val url = "${baseUrl}test-session/$sessionId/events".toHttpUrl().newBuilder()
            .addQueryParameter("clientType", "phone")
            .apply {
                inventoryToken?.let { addQueryParameter("assetInventoryToken", it) }
            }
            .build()
        StoryTestingLogger.d("NET") { "Opening SSE connection — $url, lastEventId=$lastEventId" }

        val request = Request.Builder()
            .url(url)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .header("Authorization", "Bearer $token")
            .apply {
                lastEventId?.let { header("Last-Event-ID", it) }
            }
            .build()

        // SSE streams are long-lived and the server only sends keep-alive comments every ~30s,
        // so we use a dedicated client with a read timeout larger than that interval.
        val sseClient = client.newBuilder()
            .readTimeout(SSE_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(SSE_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

        val call = sseClient.newCall(request)
        activeCall = call

        return try {
            val response = call.execute()
            if (!response.isSuccessful) {
                response.closeQuietly()
                StoryTestingLogger.w("NET") { "SSE HTTP ${response.code}" }
                if (response.code == 403 || response.code == 401) {
                    throw TestSessionException("Authentication failed: ${response.code}")
                }
                if (response.code == 404) {
                    throw TestSessionException("Test session not found")
                }
                return null
            }
            StoryTestingLogger.i("NET") { "SSE connected — HTTP ${response.code}" }
            response
        } catch (e: IOException) {
            StoryTestingLogger.e("NET", e) { "SSE connection IOException" }
            null
        }
    }

    private suspend fun kotlinx.coroutines.channels.ProducerScope<TestEvent>.readEvents(response: Response) {
        val body = response.body ?: return
        val source = body.source()

        var currentId: String? = null
        var currentEvent: String? = null
        val currentData = StringBuilder()

        while (isActive && !isClosed) {
            val line = source.readUtf8Line() ?: break

            when {
                line.startsWith("id:") -> {
                    currentId = line.substringAfter("id:").trim()
                }

                line.startsWith("event:") -> {
                    currentEvent = line.substringAfter("event:").trim()
                }

                line.startsWith("data:") -> {
                    if (currentData.isNotEmpty()) {
                        currentData.append("\n")
                    }
                    currentData.append(line.substringAfter("data:").trim())
                }

                line.startsWith(":") || line.isBlank() -> {
                    if (currentEvent != null && currentData.isNotEmpty()) {
                        currentId?.let { lastEventId = it }
                        StoryTestingLogger.d("NET") { "SSE dispatching event=$currentEvent, id=$currentId" }
                        val event = TestEventParser.parse(currentEvent, currentData.toString())
                        trySend(event)
                    }
                    currentId = null
                    currentEvent = null
                    currentData.clear()
                }
            }
        }
    }

    override fun close() {
        StoryTestingLogger.d("NET") { "Closing SSE connection" }
        isClosed = true
        activeCall?.cancel()
        activeCall = null
    }

    private companion object {
        const val RECONNECT_DELAY_MS = 3000L
        const val MAX_CONSECUTIVE_FAILURES = 5
        const val SSE_READ_TIMEOUT_SECONDS = 45L
        const val SSE_WRITE_TIMEOUT_SECONDS = 60L
    }
}
