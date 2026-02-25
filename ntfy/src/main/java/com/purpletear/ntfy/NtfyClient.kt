package com.purpletear.ntfy

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
import org.json.JSONObject
import java.io.IOException
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

    companion object {
        private const val JSON_MEDIA_TYPE = "application/json"
        private const val PRIORITY_DEFAULT = "default"
        private const val PRIORITY_HIGH = "high"
        private const val PRIORITY_URGENT = "urgent"

        private fun defaultOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }
    }

    override fun send(message: String, data: Map<String, Any?>?) {
        if (config.logChannelId.isBlank()) {
            handleError("Log channel ID not configured")
            return
        }
        sendInternal(message, config.logChannelId, PRIORITY_DEFAULT, data)
    }

    override fun send(message: String, channelId: String, data: Map<String, Any?>?) {
        if (channelId.isBlank()) {
            handleError("Channel ID cannot be blank")
            return
        }
        sendInternal(message, channelId, PRIORITY_DEFAULT, data)
    }

    override fun exception(throwable: Throwable, data: Map<String, Any?>?) {
        if (config.errorChannelId.isBlank()) {
            handleError("Error channel ID not configured")
            return
        }
        val message = buildString {
            appendLine("Exception: ${throwable.javaClass.simpleName}")
            appendLine("Message: ${throwable.message}")
            appendLine("Stacktrace:")
            append(throwable.stackTraceToString().take(2000))
        }
        val mergedData = mutableMapOf<String, Any?>()
        mergedData["exception_class"] = throwable.javaClass.name
        mergedData["exception_message"] = throwable.message
        if (data != null) {
            mergedData.putAll(data)
        }
        sendInternal(message, config.errorChannelId, PRIORITY_HIGH, mergedData)
    }

    override fun urgent(throwable: Throwable, data: Map<String, Any?>?) {
        if (config.urgentChannelId.isBlank()) {
            handleError("Urgent channel ID not configured")
            return
        }
        val message = buildString {
            appendLine("URGENT: ${throwable.javaClass.simpleName}")
            appendLine("Message: ${throwable.message}")
            appendLine("Stacktrace:")
            append(throwable.stackTraceToString().take(1500))
        }
        val mergedData = mutableMapOf<String, Any?>()
        mergedData["exception_class"] = throwable.javaClass.name
        mergedData["exception_message"] = throwable.message
        if (data != null) {
            mergedData.putAll(data)
        }
        sendInternal(message, config.urgentChannelId, PRIORITY_URGENT, mergedData)
    }

    override fun urgent(message: String, data: Map<String, Any?>?) {
        if (config.urgentChannelId.isBlank()) {
            handleError("Urgent channel ID not configured")
            return
        }
        sendInternal(message, config.urgentChannelId, PRIORITY_URGENT, data)
    }

    private fun sendInternal(
        message: String,
        channelId: String,
        priority: String,
        data: Map<String, Any?>?
    ) {
        externalScope.launch {
            try {
                val payload = JSONObject().apply {
                    put("message", message)
                    put("priority", priority)
                    if (data != null) {
                        put("data", JSONObject(data))
                    }
                }

                val request = Request.Builder()
                    .url("${config.baseUrl}/$channelId")
                    .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE.toMediaType()))
                    .header("Content-Type", JSON_MEDIA_TYPE)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        handleError("Failed to send notification: ${e.message}", e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.close()
                        if (!response.isSuccessful) {
                            handleError("Notification failed with status: ${response.code}")
                        }
                    }
                })
            } catch (e: Exception) {
                handleError("Error sending notification: ${e.message}", e)
            }
        }
    }

    private fun handleError(message: String, cause: Throwable? = null) {
        if (config.silent) {
            // Silently log the error (in production you might want to use a logger)
            cause?.printStackTrace()
        } else {
            throw NtfyException(message, cause ?: Exception(message))
        }
    }
}
