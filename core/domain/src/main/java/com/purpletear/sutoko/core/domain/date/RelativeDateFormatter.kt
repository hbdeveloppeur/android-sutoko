package com.purpletear.sutoko.core.domain.date

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.purpletear.sutoko.core.domain.R as CoreR

object RelativeDateFormatter {

    fun formatNewsDate(
        context: Context,
        epoch: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): String {
        val targetMillis = toMillis(epoch)
        val todayStart = startOfDayMillis(nowMillis)
        val targetStart = startOfDayMillis(targetMillis)
        val diffDays = ((targetStart - todayStart) / MILLIS_PER_DAY).toInt()

        val text = when {
            diffDays < 0 -> formatAbsoluteDate(targetMillis, Locale.getDefault())
            diffDays == 0 -> context.getString(CoreR.string.date_relative_today)
            diffDays == 1 -> context.getString(CoreR.string.date_relative_tomorrow)
            diffDays == 2 -> context.getString(CoreR.string.date_relative_day_after_tomorrow)
            diffDays in 3..6 -> context.resources.getQuantityString(
                CoreR.plurals.date_relative_in_days,
                diffDays,
                diffDays
            )

            else -> formatAbsoluteDate(targetMillis, Locale.getDefault())
        }
        return capitalizeFirst(text, Locale.getDefault())
    }

    // Returns true if the given epoch (seconds or millis) is the same calendar day as 'nowMillis' or earlier.
    fun isTodayOrEarlier(epoch: Long, nowMillis: Long = System.currentTimeMillis()): Boolean {
        val targetMillis = toMillis(epoch)
        return startOfDayMillis(targetMillis) <= startOfDayMillis(nowMillis)
    }

    private fun toMillis(epoch: Long): Long {
        return if (epoch < 1_000_000_000_000L) epoch * 1000 else epoch
    }

    private fun startOfDayMillis(timeMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun formatAbsoluteDate(timeMillis: Long, locale: Locale): String {
        val pattern = "EEEE d MMMM"
        val sdf = SimpleDateFormat(pattern, locale)
        return sdf.format(Date(timeMillis))
    }

    private fun capitalizeFirst(text: String, locale: Locale): String {
        if (text.isEmpty()) return text
        return text.replaceFirstChar { ch ->
            if (ch.isLowerCase()) ch.titlecase(locale) else ch.toString()
        }
    }

    private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
}