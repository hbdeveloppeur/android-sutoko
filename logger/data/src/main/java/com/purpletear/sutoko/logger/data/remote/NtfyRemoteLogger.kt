package com.purpletear.sutoko.logger.data.remote

import com.purpletear.sutoko.core.domain.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote logger that posts messages to ntfy.sh topics.
 *
 * This class is fire-and-forget: callers are never blocked, and network
 * failures are silently dropped.
 */
@Singleton
class NtfyRemoteLogger @Inject constructor(
    private val config: LoggerConfig
) : Logger {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .cache(null)
        .build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun warning(message: String, data: Map<String, String>) {
        send(config.logChannel, message, data)
    }

    override fun exception(
        throwable: Throwable,
        message: String?,
        data: Map<String, String>
    ) {
        val body = buildString {
            message?.let { appendLine(it).appendLine() }
            append(throwable.stackTraceToString())
            appendData(data + ("exception" to throwable.javaClass.simpleName))
        }
        send(config.exceptionChannel, body, emptyMap())
    }

    private fun send(channel: String, message: String, data: Map<String, String>) {
        val body = if (data.isEmpty()) {
            message
        } else {
            buildString {
                append(message)
                appendData(data)
            }
        }

        scope.launch {
            runCatching {
                val request = Request.Builder()
                    .url("${config.baseUrl}/$channel")
                    .post(body.toRequestBody(CONTENT_TYPE_PLAIN))
                    .build()
                client.newCall(request).execute().close()
            }
        }
    }

    private fun StringBuilder.appendData(data: Map<String, String>) {
        if (data.isEmpty()) return
        appendLine().appendLine("---")
        data.forEach { (key, value) ->
            appendLine("$key: $value")
        }
    }

    companion object {
        private val CONTENT_TYPE_PLAIN = "text/plain; charset=utf-8".toMediaType()
    }
}

/**
 * Build-time configuration for the remote logger.
 */
data class LoggerConfig(
    val exceptionChannel: String,
    val logChannel: String,
    val baseUrl: String = "https://ntfy.sh"
)
