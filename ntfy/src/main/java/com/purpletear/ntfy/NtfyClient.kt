package com.purpletear.ntfy

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Ntfy client implementation for sending notifications via ntfy.sh
 *
 * @param config The configuration for the client
 * @param client Optional OkHttpClient instance (a default one will be created if not provided)
 * @param externalScope Optional CoroutineScope for async operations (defaults to IO dispatcher)
 */
class NtfyClient(
    private val config: NtfyConfig,
    private val client: OkHttpClient = defaultOkHttpClient(),
    private val externalScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : Ntfy {

    private var currentAction: String? = null

    companion object {
        private const val TEXT_MEDIA_TYPE = "text/plain; charset=utf-8"
        private const val PRIORITY_DEFAULT = "default"
        private const val PRIORITY_HIGH = "high"
        private const val PRIORITY_URGENT = "urgent"
        
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault())

        private fun defaultOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }
    }

    override fun startAction(description: String) {
        currentAction = description
    }

    override fun send(message: String, data: Map<String, Any?>?) {
        Log.d("Ntfy", "send() called, channel='${config.logChannelId}'")
        if (config.logChannelId.isBlank()) {
            Log.w("Ntfy", "Log channel ID not configured")
            handleError("Log channel ID not configured")
            return
        }
        val formatted = formatLogMessage(message, data)
        sendInternal(formatted, config.logChannelId, PRIORITY_DEFAULT)
    }

    override fun send(message: String, channelId: String, data: Map<String, Any?>?) {
        if (channelId.isBlank()) {
            handleError("Channel ID cannot be blank")
            return
        }
        val formatted = formatLogMessage(message, data)
        sendInternal(formatted, channelId, PRIORITY_DEFAULT)
    }

    override fun exception(throwable: Throwable, data: Map<String, Any?>?) {
        if (config.errorChannelId.isBlank()) {
            handleError("Error channel ID not configured")
            return
        }
        val formatted = formatExceptionMessage(throwable, data)
        sendInternal(formatted, config.errorChannelId, PRIORITY_HIGH)
    }

    override fun urgent(throwable: Throwable, data: Map<String, Any?>?) {
        if (config.urgentChannelId.isBlank()) {
            handleError("Urgent channel ID not configured")
            return
        }
        val formatted = formatUrgentExceptionMessage(throwable, data)
        sendInternal(formatted, config.urgentChannelId, PRIORITY_URGENT)
    }

    override fun urgent(message: String, data: Map<String, Any?>?) {
        if (config.urgentChannelId.isBlank()) {
            handleError("Urgent channel ID not configured")
            return
        }
        val formatted = formatUrgentMessage(message, data)
        sendInternal(formatted, config.urgentChannelId, PRIORITY_URGENT)
    }

    private fun formatLogMessage(message: String, data: Map<String, Any?>?): String {
        return buildString {
            this@NtfyClient.currentAction?.let {
                action ->
                appendLine("Action: $action")
            }
            appendLine(message)
            if (data != null && data.isNotEmpty()) {
                appendLine()
                data.entries.joinToString(" | ") { "${it.key}=${it.value}" }.let {
                    append("• $it")
                }
            }
        }
    }

    private fun formatExceptionMessage(throwable: Throwable, data: Map<String, Any?>?): String {
        return buildString {
            this@NtfyClient.currentAction?.let {
                    action ->
                appendLine("Action: $action")
            }
            appendLine("${throwable.javaClass.simpleName}: ${throwable.message}")
            appendLine()
            appendLine(getRootCause(throwable))
            append("—".repeat(20))
            appendLine()
            append(throwable.stackTrace.firstOrNull()?.let { 
                "at ${it.className}.${it.methodName} (${it.fileName}:${it.lineNumber})"
            } ?: "No stack trace")
            
            if (data != null && data.isNotEmpty()) {
                appendLine()
                appendLine("—".repeat(20))
                data.entries.joinToString(" | ") { "${it.key}=${it.value}" }.let {
                    append("• $it")
                }
            }
        }
    }

    private fun formatUrgentExceptionMessage(throwable: Throwable, data: Map<String, Any?>?): String {
        return buildString {
            this@NtfyClient.currentAction?.let {
                    action ->
                appendLine("Action: $action")
            }
            appendLine("URGENT: ${throwable.javaClass.simpleName}")
            appendLine("${throwable.message}")
            appendLine()
            appendLine(getRootCause(throwable))
            append("—".repeat(20))
            appendLine()
            appendLine(throwable.stackTrace.take(3).joinToString("\n") { 
                "• ${it.className}.${it.methodName}"
            })
            
            if (data != null && data.isNotEmpty()) {
                appendLine()
                append("—".repeat(20))
                appendLine()
                data.entries.joinToString("\n") { "• ${it.key}: ${it.value}" }.let {
                    append(it)
                }
            }
        }
    }

    private fun formatUrgentMessage(message: String, data: Map<String, Any?>?): String {
        return buildString {
            this@NtfyClient.currentAction?.let {
                    action ->
                appendLine("Action: $action")
            }
            appendLine(message)
            if (data != null && data.isNotEmpty()) {
                appendLine()
                append("—".repeat(20))
                appendLine()
                data.entries.joinToString("\n") { "• ${it.key}: ${it.value}" }.let {
                    append(it)
                }
            }
        }
    }

    private fun getRootCause(throwable: Throwable): String {
        var cause = throwable
        while (cause.cause != null && cause.cause !== cause) {
            cause = cause.cause!!
        }
        return if (cause !== throwable) "Caused by: ${cause.javaClass.simpleName}: ${cause.message}" else ""
    }

    private fun sendInternal(
        message: String,
        channelId: String,
        priority: String
    ) {
        externalScope.launch {
            try {
                val timestamp = timeFormatter.format(Instant.now())
                val title = "${timestamp} ${config.appName ?: "App"}"

                val url = "${config.baseUrl}/$channelId"
                val body = message.toRequestBody(TEXT_MEDIA_TYPE.toMediaType())
                
                Log.d("Ntfy", "Sending to $url")
                Log.d("Ntfy", "Title: $title")
                Log.d("Ntfy", "Message: $message")

                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .header("Priority", priority)
                    .apply {
                        if (priority == PRIORITY_URGENT) {
                            header("Tags", "warning")
                        }
                    }
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("Ntfy", "Failed to send: ${e.message}", e)
                        handleError("Failed to send notification: ${e.message}", e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val body = response.body?.string() ?: "empty"
                        Log.d("Ntfy", "Response: ${response.code} - $body")
                        response.close()
                        if (!response.isSuccessful) {
                            handleError("Notification failed with status: ${response.code}")
                        }
                    }
                })
            } catch (e: Exception) {
                Log.e("Ntfy", "Error: ${e.message}", e)
                handleError("Error sending notification: ${e.message}", e)
            }
        }
    }

    private fun handleError(message: String, cause: Throwable? = null) {
        if (config.silent) {
            cause?.printStackTrace()
        } else {
            throw NtfyException(message, cause ?: Exception(message))
        }
    }
}
