package com.purpletear.core.date

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object DateUtils {
    fun formatTimestampToDate(timestampSeconds: Long, format: String = "dd/MM/yyyy"): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(Date(timestampSeconds * 1000))
    }

    fun daysBetween(timestamp1: Long, timestamp2: Long): Long {
        val diff = timestamp2 - timestamp1
        return diff / (24 * 60 * 60 * 1000)
    }
}