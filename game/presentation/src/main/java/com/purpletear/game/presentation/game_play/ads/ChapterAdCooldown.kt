package com.purpletear.game.presentation.game_play.ads

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChapterAdCooldown @Inject constructor(@ApplicationContext context: Context) {
    private val preferences = context.getSharedPreferences("chapter_ads", Context.MODE_PRIVATE)

    fun remainingMillis(now: Long = System.currentTimeMillis()): Long =
        remainingCooldown(preferences.getLong("last_reward_at", 0L), now)

    fun recordReward() {
        preferences.edit().putLong("last_reward_at", System.currentTimeMillis()).apply()
    }
}

internal const val CHAPTER_AD_COOLDOWN_MS = 5 * 60 * 1000L

internal fun remainingCooldown(lastReward: Long, now: Long): Long = when {
    lastReward <= 0L -> 0L
    now < lastReward -> CHAPTER_AD_COOLDOWN_MS
    else -> (CHAPTER_AD_COOLDOWN_MS - (now - lastReward)).coerceAtLeast(0L)
}
