package com.purpletear.sutoko.analytics.data.statistics

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.purpletear.sutoko.analytics.data.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class StatisticsWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        if(BuildConfig.DEBUG && !StatisticsQa.enabled(applicationContext)) return Result.success()
        runCatching {
            StatisticsStore(applicationContext).use { store ->
                StatisticsDelivery.attempt(store,System.currentTimeMillis()/1000,Random.nextLong(0,301),::upload)
            }
        }
        // Periodic work and a persisted budget handle retries; never hot-loop Result.retry().
        return Result.success()
    }
    private fun upload(batch: List<ChapterReport>): UploadOutcome {
        val request=Request.Builder().url("https://stats.sutoko.com/api/v1/reports")
            .post(StatisticsPayload.encode(batch,if(StatisticsQa.enabled(applicationContext)) "qa" else "production").toRequestBody("application/json".toMediaType()))
            .apply { if(StatisticsQa.enabled(applicationContext)) header("X-Statistics-QA-Key",StatisticsQa.key(applicationContext)) }.build()
        client.newCall(request).execute().use { response ->
            return when {
                response.code == 202 -> UploadOutcome.Accepted
                DeliveryPolicy.isPermanentFailure(response.code) -> UploadOutcome.Rejected
                else -> UploadOutcome.Unavailable(response.header("Retry-After")?.toLongOrNull() ?: 0)
            }
        }
    }
    companion object {
        private val client by lazy { OkHttpClient.Builder()
            .connectTimeout(3,TimeUnit.SECONDS).readTimeout(3,TimeUnit.SECONDS).writeTimeout(3,TimeUnit.SECONDS)
            .callTimeout(5,TimeUnit.SECONDS).followRedirects(false).followSslRedirects(false)
            .retryOnConnectionFailure(false).build() }
    }
}
