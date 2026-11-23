package com.purpletear.ads

import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import javax.inject.Inject

internal class AdFrequencyStore @Inject constructor(@ApplicationContext private val context: Context) {
    fun boot() = Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1)
    fun now() = SystemClock.elapsedRealtime()

    // All preference reads and durable writes run on Dispatchers.IO.
    fun load(): Pair<AdFrequencyState, Int> {
        val preferences = context.getSharedPreferences("reader_ads", Context.MODE_PRIVATE)
        val now = now()
        val boot = boot()
        val state = try {
            preferences.getString("frequency", null)?.let { raw ->
                val json = JSONObject(raw)
                val shows = json.getJSONArray("shows")
                val observed = json.getLong("observed")
                require(observed >= 0)
                require(shows.length() <= 2)
                AdFrequencyState(json.getInt("boot"), json.getLong("observed"),
                    (0 until shows.length()).map { i ->
                        val show = shows.getJSONObject(i)
                        AdShow(AdFormat.valueOf(show.getString("format")), show.getLong("at").also { require(it in 0..observed) }, show.optBoolean("pending", false))
                    }, if (json.has("dismissed")) json.getLong("dismissed").also { require(it in 0..observed) } else null)
            } ?: migrateReward(boot, now)
        } catch (_: Exception) {
            // Corruption must not give the installation a fresh budget.
            AdFrequencyState(boot, now, List(2) { AdShow(AdFormat.INTERSTITIAL, now) }, now)
        }
        val bucket = preferences.getInt("bucket", -1).takeIf { it in 0..99 } ?: SecureRandom().nextInt(100)
        check(preferences.edit().putInt("bucket", bucket).commit()) { "Unable to persist ad cohort" }
        return AdFrequencyPolicy(state).apply {
            reconcile(boot, now, forceRebase = boot < 0)
            recoverPending(now)
        }.state to bucket
    }

    private fun migrateReward(boot: Int, now: Long): AdFrequencyState {
        val last = context.getSharedPreferences("chapter_ads", Context.MODE_PRIVATE).getLong("last_reward_at", 0)
        val age = (System.currentTimeMillis() - last).coerceAtLeast(0)
        val shows = if (last > 0 && age < AD_WINDOW_MS) listOf(AdShow(AdFormat.REWARDED, (now - age).coerceAtLeast(0))) else emptyList()
        return AdFrequencyState(boot, now, shows, shows.lastOrNull()?.elapsedMs)
    }

    fun save(state: AdFrequencyState): Boolean {
        val json = JSONObject().put("boot", state.boot).put("observed", state.observedAt)
            .put("shows", JSONArray().apply {
                state.shows.forEach { put(JSONObject().put("format", it.format.name).put("at", it.elapsedMs).put("pending", it.pending)) }
            })
        state.dismissedAt?.let { json.put("dismissed", it) }
        return context.getSharedPreferences("reader_ads", Context.MODE_PRIVATE).edit()
            .putString("frequency", json.toString()).commit()
    }
}
