package com.purpletear.game.data.remote.testing

import com.google.gson.Gson
import com.purpletear.game.data.remote.testing.dto.SseConnectedPayload
import com.purpletear.game.data.remote.testing.dto.SseErrorPayload
import com.purpletear.game.data.remote.testing.dto.SsePhonePayload
import com.purpletear.game.data.remote.testing.dto.SsePlayFromNodePayload
import com.purpletear.game.data.remote.testing.dto.SseSeedUpdatedPayload
import com.purpletear.sutoko.game.model.testing.TestEvent
import com.purpletear.sutoko.game.testing.StoryTestingLogger

internal object TestEventParser {

    private val gson = Gson()

    fun parse(eventType: String, data: String): TestEvent {
        return try {
            when (eventType) {
                "CONNECTED" -> {
                    val payload = gson.fromJson(data, SseConnectedPayload::class.java)
                    StoryTestingLogger.d("NET") { "Parsed CONNECTED — sessionId=${payload.sessionId}, seeds=${payload.chapterSeeds}" }
                    TestEvent.Connected(
                        sessionId = payload.sessionId,
                        chapterSeeds = payload.chapterSeeds
                    )
                }

                "PHONE_CONNECTED" -> {
                    val payload = gson.fromJson(data, SsePhonePayload::class.java)
                    TestEvent.PhoneConnected(
                        phoneId = payload.phoneId,
                        deviceInfo = payload.deviceInfo
                    )
                }

                "PHONE_DISCONNECTED" -> {
                    val payload = gson.fromJson(data, SsePhonePayload::class.java)
                    TestEvent.PhoneDisconnected(phoneId = payload.phoneId)
                }

                "SEED_UPDATED" -> {
                    val payload = gson.fromJson(data, SseSeedUpdatedPayload::class.java)
                    StoryTestingLogger.d("NET") { "Parsed SEED_UPDATED — ${payload.chapterId}: ${payload.seed}" }
                    TestEvent.SeedUpdated(
                        chapterId = payload.chapterId,
                        seed = payload.seed,
                        packageUrl = payload.packageUrl,
                        changedAssets = payload.changedAssets
                    )
                }

                "PLAY_FROM_NODE" -> {
                    val payload = gson.fromJson(data, SsePlayFromNodePayload::class.java)
                    StoryTestingLogger.d("NET") { "Parsed PLAY_FROM_NODE — ${payload.chapterId} → ${payload.nodeId} (seed=${payload.seedAtRequest})" }
                    TestEvent.PlayFromNode(
                        chapterId = payload.chapterId,
                        nodeId = payload.nodeId,
                        seedAtRequest = payload.seedAtRequest
                    )
                }

                "ERROR" -> {
                    val payload = gson.fromJson(data, SseErrorPayload::class.java)
                    TestEvent.Error(
                        code = payload.code,
                        message = payload.message
                    )
                }

                else -> {
                    StoryTestingLogger.w("NET") { "Unknown SSE event type: $eventType" }
                    TestEvent.Error("unknown_event", "Unknown event type: $eventType")
                }
            }
        } catch (e: Exception) {
            StoryTestingLogger.e("NET", e) { "Failed to parse SSE event $eventType" }
            TestEvent.Error("parse_error", "Failed to parse $eventType: ${e.message}")
        }
    }
}
