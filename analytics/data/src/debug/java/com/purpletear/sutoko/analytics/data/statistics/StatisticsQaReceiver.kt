package com.purpletear.sutoko.analytics.data.statistics
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.Executors
class StatisticsQaReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context,intent: Intent) {
        val pending=goAsync()
        val executor=Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                when(intent.action) {
                    "com.purpletear.statistics.CONFIGURE_QA" -> {
                        val key=intent.getStringExtra("key") ?: return@execute
                        StatisticsQa.configure(context,key)
                        if(StatisticsQa.enabled(context)) StatisticsStore(context).use { it.setEnabled(true) }
                    }
                    "com.purpletear.statistics.FLUSH_QA" -> if(StatisticsQa.enabled(context)) {
                        // Explicit QA flush only; production upload budget is never bypassed.
                        StatisticsStore(context).use { it.writableDatabase.execSQL("UPDATE settings SET next_allowed=0 WHERE id=1") }
                        WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<StatisticsWorker>().build())
                    }
                }
            } finally { pending.finish(); executor.shutdown() }
        }
    }
}
