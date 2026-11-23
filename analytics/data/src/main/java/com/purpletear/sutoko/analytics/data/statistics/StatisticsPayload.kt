package com.purpletear.sutoko.analytics.data.statistics
import org.json.JSONArray
import org.json.JSONObject
internal object StatisticsPayload {
    fun encode(batch: List<ChapterReport>,source: String = "production"): String = JSONObject().apply {
        put("version",2); put("source",source)
        put("events",JSONArray().apply { batch.forEach { e -> put(JSONObject().apply {
            put("event_id",e.eventId); put("run_id",e.runId); put("visitor_id",e.visitorId)
            put("preview_id",e.previewId ?: JSONObject.NULL); put("engine",e.key.engine); put("game_id",e.key.gameId)
            put("language",e.key.language); put("chapter",e.key.chapter); put("kind",e.kind); put("at",e.at)
            put("replay",e.replay); put("mode",e.mode); put("reason",e.reason)
        }) } })
    }.toString()
}
