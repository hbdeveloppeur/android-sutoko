package com.purpletear.sutoko.analytics.data.statistics

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.UUID

/** Private, bounded storage. All callers use background threads. No gameplay state is stored here. */
internal class StatisticsStore(context: Context, name: String = DATABASE_NAME) : SQLiteOpenHelper(context, name, null, 3) {
    companion object { const val DATABASE_NAME = "sutoko-statistics.db" }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE settings (id INTEGER PRIMARY KEY CHECK(id=1), enabled INTEGER NOT NULL, next_allowed INTEGER NOT NULL, failures INTEGER NOT NULL)")
        db.execSQL("INSERT INTO settings VALUES(1,1,0,0)")
        db.execSQL("CREATE TABLE sessions (engine TEXT NOT NULL, game_id TEXT NOT NULL, language TEXT NOT NULL, run_id TEXT NOT NULL, created_at INTEGER NOT NULL, replay INTEGER NOT NULL, PRIMARY KEY(engine,game_id,language))")
        db.execSQL("CREATE TABLE chapters (run_id TEXT NOT NULL, chapter TEXT NOT NULL, completed INTEGER NOT NULL, last_active INTEGER NOT NULL, PRIMARY KEY(run_id,chapter))")
        db.execSQL("CREATE TABLE events (event_id TEXT PRIMARY KEY, run_id TEXT NOT NULL, engine TEXT NOT NULL, game_id TEXT NOT NULL, language TEXT NOT NULL, chapter TEXT NOT NULL, kind TEXT NOT NULL, at INTEGER NOT NULL, replay INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX event_time ON events(at)")
        createGrowth(db)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) createGrowth(db)
    }
    private fun createGrowth(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE events ADD COLUMN visitor_id TEXT")
        db.execSQL("ALTER TABLE events ADD COLUMN preview_id TEXT")
        db.execSQL("ALTER TABLE events ADD COLUMN mode TEXT NOT NULL DEFAULT 'full'")
        db.execSQL("ALTER TABLE events ADD COLUMN reason TEXT NOT NULL DEFAULT ''")
        db.execSQL("CREATE TABLE journeys (engine TEXT NOT NULL, game_id TEXT NOT NULL, language TEXT NOT NULL, visitor_id TEXT NOT NULL, created_at INTEGER NOT NULL, run_id TEXT NOT NULL, started INTEGER NOT NULL, finished INTEGER NOT NULL, replay INTEGER NOT NULL, preview_id TEXT, preview_at INTEGER NOT NULL, last_session INTEGER NOT NULL, last_seen INTEGER NOT NULL, mode TEXT NOT NULL, PRIMARY KEY(engine,game_id,language))")
    }

    fun isEnabled(): Boolean = readableDatabase.rawQuery("SELECT enabled FROM settings WHERE id=1", null).use { it.moveToFirst() && it.getInt(0) == 1 }

    fun setEnabled(enabled: Boolean) = transaction { db ->
        db.execSQL("UPDATE settings SET enabled=? WHERE id=1", arrayOf(if (enabled) 1 else 0))
        if (!enabled) {
            db.delete("events", null, null); db.delete("chapters", null, null); db.delete("sessions", null, null); db.delete("journeys",null,null)
            // Preserve next_allowed so opt-out/opt-in cannot bypass the upload budget.
        }
    }

    fun record(key: ChapterKey, kind: String, now: Long, mode: String = "full", reason: String = ""): Boolean = transaction { db ->
        if(!isEnabled()) return@transaction false
        prune(db,now)
        val args=arrayOf(key.engine,key.gameId,key.language)
        var j=db.rawQuery("SELECT visitor_id,created_at,run_id,started,finished,replay,preview_id,preview_at,last_session,last_seen,mode FROM journeys WHERE engine=? AND game_id=? AND language=?",args).use {
            if(it.moveToFirst()) Journey(it.getString(0),it.getLong(1),it.getString(2),it.getInt(3)==1,it.getInt(4)==1,it.getInt(5)==1,if(it.isNull(6)) null else it.getString(6),it.getLong(7),it.getLong(8),it.getLong(9),it.getString(10)) else null
        }
        if(j==null) j=Journey(UUID.randomUUID().toString(),now,UUID.randomUUID().toString(),false,false,false,null,0,0,now,mode)
        var created=false
        fun append(eventKind: String,chapter: String=key.chapter,eventMode: String=mode,preview: String?=j!!.previewId): String {
            val id=UUID.randomUUID().toString()
            db.insertOrThrow("events",null,ContentValues().apply {
                put("event_id",id);put("run_id",j!!.runId);put("visitor_id",j!!.visitorId);put("preview_id",preview)
                put("engine",key.engine);put("game_id",key.gameId);put("language",key.language);put("chapter",chapter)
                put("kind",eventKind);put("at",now/60*60);put("replay",if(j!!.replay) 1 else 0);put("mode",eventMode);put("reason",reason)
            });created=true;return id
        }
        fun session() {
            val fromPreview=j!!.previewId!=null && j!!.previewAt>j!!.lastSession
            if(j!!.lastSession==0L || now-j!!.lastSeen>=1800 || fromPreview) { append("session_start");j=j!!.copy(lastSession=now) }
        }
        when(kind) {
            "restart" -> { j=j!!.copy(runId=UUID.randomUUID().toString(),started=false,finished=false,replay=true,lastSession=0); }
            "preview" -> {
                if(j!!.finished) j=j!!.copy(runId=UUID.randomUUID().toString(),started=false,finished=false,replay=true,lastSession=0)
                // Explicit calls are per preview ViewModel, not every recomposition.
                val id=append("preview",chapter="",eventMode="all",preview=null);j=j!!.copy(previewId=id,previewAt=now)
            }
            "play_click" -> append("play_click",chapter="")
            "start" -> {
                val complete=db.rawQuery("SELECT completed FROM chapters WHERE run_id=? AND chapter=?",arrayOf(j!!.runId,key.chapter)).use { it.moveToFirst() && it.getInt(0)==1 }
                if(complete || j!!.finished || (j!!.started && j!!.mode!=mode)) j=j!!.copy(runId=UUID.randomUUID().toString(),started=false,finished=false,replay=true,lastSession=0)
                if(!j!!.started) { append("story_start",chapter="");j=j!!.copy(started=true,mode=mode) }
                session()
                val existing=db.rawQuery("SELECT completed,last_active FROM chapters WHERE run_id=? AND chapter=?",arrayOf(j!!.runId,key.chapter)).use { if(it.moveToFirst()) Pair(it.getInt(0)==1,it.getLong(1)) else null }
                if(existing==null) { db.execSQL("INSERT INTO chapters VALUES(?,?,0,?)",arrayOf(j!!.runId,key.chapter,now));append("start") }
                else if(!existing.first && now-existing.second>=86400) append("active")
            }
            "active" -> {
                if(j!!.started && !j!!.finished) {
                    session()
                    val last=db.rawQuery("SELECT last_active FROM chapters WHERE run_id=? AND chapter=?",arrayOf(j!!.runId,key.chapter)).use { if(it.moveToFirst()) it.getLong(0) else null }
                    if(last!=null && now-last>=86400) append("active",eventMode=j!!.mode)
                }
            }
            "complete" -> {
                val done=db.rawQuery("SELECT completed FROM chapters WHERE run_id=? AND chapter=?",arrayOf(j!!.runId,key.chapter)).use { if(it.moveToFirst()) it.getInt(0)==1 else null }
                if(done==false) { append("complete",eventMode=j!!.mode);db.execSQL("UPDATE chapters SET completed=1 WHERE run_id=? AND chapter=?",arrayOf(j!!.runId,key.chapter)) }
            }
            "story_complete", "trial_end" -> { if(j!!.started && !j!!.finished) { append(if(j!!.mode=="trial") "trial_end" else "story_complete",eventMode=j!!.mode);j=j!!.copy(finished=true) } }
            "failure" -> append("failure")
            "next_available", "next_unavailable", "progress" -> if(j!!.started) {
                val duplicate=db.rawQuery("SELECT 1 FROM events WHERE run_id=? AND chapter=? AND kind=? AND reason=? LIMIT 1",arrayOf(j!!.runId,key.chapter,kind,reason)).use { it.moveToFirst() }
                if(!duplicate) append(kind,eventMode=j!!.mode)
            }
            else -> return@transaction false
        }
        if(kind!="preview" && kind!="play_click") j=j!!.copy(lastSeen=now)
        if(kind in setOf("start","active","complete")) db.execSQL("UPDATE chapters SET last_active=? WHERE run_id=? AND chapter=?",arrayOf(now,j!!.runId,key.chapter))
        db.insertWithOnConflict("journeys",null,ContentValues().apply {
            put("engine",key.engine);put("game_id",key.gameId);put("language",key.language);put("visitor_id",j!!.visitorId);put("created_at",j!!.createdAt);put("run_id",j!!.runId)
            put("started",if(j!!.started) 1 else 0);put("finished",if(j!!.finished) 1 else 0);put("replay",if(j!!.replay) 1 else 0);put("preview_id",j!!.previewId);put("preview_at",j!!.previewAt)
            put("last_session",j!!.lastSession);put("last_seen",j!!.lastSeen);put("mode",j!!.mode)
        },SQLiteDatabase.CONFLICT_REPLACE)
        db.execSQL("DELETE FROM events WHERE event_id IN (SELECT event_id FROM events ORDER BY at DESC,rowid DESC LIMIT -1 OFFSET ${DeliveryPolicy.MAX_QUEUE})")
        db.execSQL("DELETE FROM chapters WHERE run_id IN (SELECT run_id FROM journeys ORDER BY created_at DESC LIMIT -1 OFFSET 64)")
        db.execSQL("DELETE FROM journeys WHERE run_id IN (SELECT run_id FROM journeys ORDER BY created_at DESC LIMIT -1 OFFSET 64)")
        created
    }

    fun reserveBatch(now: Long): List<ChapterReport> = transaction { db ->
        prune(db,now)
        val allowed = db.rawQuery("SELECT enabled,next_allowed FROM settings WHERE id=1",null).use { it.moveToFirst(); it.getInt(0)==1 && now>=it.getLong(1) }
        if(!allowed) return@transaction emptyList()
        val batch = db.rawQuery("SELECT event_id,run_id,engine,game_id,language,chapter,kind,at,replay,visitor_id,preview_id,mode,reason FROM events ORDER BY at,rowid LIMIT ${DeliveryPolicy.MAX_BATCH}",null).use { cursor ->
            buildList { while(cursor.moveToNext()) add(ChapterReport(cursor.getString(0),cursor.getString(1),ChapterKey(cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5)),cursor.getString(6),cursor.getLong(7),cursor.getInt(8)==1,if(cursor.isNull(9)) cursor.getString(1) else cursor.getString(9),if(cursor.isNull(10)) null else cursor.getString(10),cursor.getString(11),cursor.getString(12))) }
        }
        if(batch.isNotEmpty()) db.execSQL("UPDATE settings SET next_allowed=? WHERE id=1",arrayOf(now+DeliveryPolicy.HOUR))
        batch
    }
    fun acknowledge(batch: List<ChapterReport>) = transaction { db ->
        batch.forEach { db.delete("events","event_id=?",arrayOf(it.eventId)) }
        db.execSQL("UPDATE settings SET failures=0 WHERE id=1")
    }
    fun failed(now: Long, retryAfter: Long, jitter: Long) = transaction { db ->
        val failures = db.rawQuery("SELECT failures FROM settings WHERE id=1",null).use { it.moveToFirst(); (it.getInt(0)+1).coerceAtMost(8) }
        db.execSQL("UPDATE settings SET failures=?,next_allowed=? WHERE id=1",arrayOf(failures,now+DeliveryPolicy.backoff(failures,retryAfter,jitter)))
    }
    private fun prune(db: SQLiteDatabase, now: Long) {
        db.delete("events","at < ? OR at > ?",arrayOf((now-DeliveryPolicy.QUEUE_TTL).toString(),(now+300).toString()))
        db.delete("chapters","run_id IN (SELECT run_id FROM sessions WHERE created_at <= ?)",arrayOf((now-DeliveryPolicy.RUN_TTL).toString()))
        db.delete("sessions","created_at <= ?",arrayOf((now-DeliveryPolicy.RUN_TTL).toString()))
        db.delete("chapters","run_id IN (SELECT run_id FROM journeys WHERE created_at <= ?)",arrayOf((now-DeliveryPolicy.RUN_TTL).toString()))
        db.delete("journeys","created_at <= ?",arrayOf((now-DeliveryPolicy.RUN_TTL).toString()))
    }
    private fun <T> transaction(block: (SQLiteDatabase)->T): T {
        val db=writableDatabase; db.beginTransaction()
        return try { block(db).also { db.setTransactionSuccessful() } } finally { db.endTransaction() }
    }
    private data class Journey(val visitorId: String,val createdAt: Long,val runId: String,val started: Boolean,val finished: Boolean,val replay: Boolean,val previewId: String?,val previewAt: Long,val lastSession: Long,val lastSeen: Long,val mode: String)
    private data class Session(val id: String,val createdAt: Long,val replay: Boolean)
}
