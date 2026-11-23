package com.purpletear.sutoko.analytics.data.statistics

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.purpletear.sutoko.core.domain.analytics.ChapterStatistics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatchedChapterStatistics @Inject constructor(@ApplicationContext private val context: Context) : ChapterStatistics {
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
    private val operations=Channel<Operation>(64)
    private val mutableEnabled=MutableStateFlow(true)
    override val enabled: StateFlow<Boolean> = mutableEnabled.asStateFlow()
    private var scheduled=false
    override val qaEnabled: StateFlow<Boolean> = MutableStateFlow(StatisticsQa.enabled(context))

    init {
        scope.launch {
            runCatching {
                StatisticsStore(context).use { store ->
                    mutableEnabled.value=store.isEnabled()
                    if(mutableEnabled.value) schedule()
                    for(op in operations) runCatching {
                        when(op) {
                            is Operation.Consent -> {
                                store.setEnabled(op.value); mutableEnabled.value=op.value
                                if(op.value) schedule() else {
                                    WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
                                    scheduled=false
                                }
                            }
                            is Operation.Event -> if(mutableEnabled.value && store.record(op.key,op.kind,System.currentTimeMillis()/1000,op.mode,op.reason)) schedule()
                        }
                    }
                }
            }.onFailure { mutableEnabled.value=false; operations.close() }
        }
    }
    override fun setEnabled(enabled: Boolean) {
        if(!enabled) mutableEnabled.value=false
        scope.launch { runCatching { operations.send(Operation.Consent(enabled)) } }
    }
    override fun entered(engine: String,gameId: String,language: String,chapter: String) = offer(engine,gameId,language,chapter,"start")
    override fun active(engine: String,gameId: String,language: String,chapter: String) = offer(engine,gameId,language,chapter,"active")
    override fun completed(engine: String,gameId: String,language: String,chapter: String) = offer(engine,gameId,language,chapter,"complete")
    override fun preview(engine: String,gameId: String,language: String) = offer(engine,gameId,language,"","preview")
    override fun playClicked(engine: String,gameId: String,language: String,isTrial: Boolean) = offer(engine,gameId,language,"","play_click",if(isTrial) "trial" else "full")
    override fun storyFinished(engine: String,gameId: String,language: String,chapter: String,isTrial: Boolean) = offer(engine,gameId,language,chapter,if(isTrial) "trial_end" else "story_complete",if(isTrial) "trial" else "full")
    override fun boundary(engine: String,gameId: String,language: String,chapter: String,nextChapter: String,available: Boolean) = offer(engine,gameId,language,chapter,if(available) "next_available" else "next_unavailable","full",nextChapter.lowercase(java.util.Locale.ROOT))
    override fun progress(engine: String,gameId: String,language: String,chapter: String,milestone: String) = offer(engine,gameId,language,chapter,"progress","full",milestone)
    override fun restarted(engine: String,gameId: String,language: String) = offer(engine,gameId,language,"","restart")
    override fun failed(engine: String,gameId: String,language: String,chapter: String,reason: String) = offer(engine,gameId,language,chapter,"failure","full",reason)
    override fun enteredTrial(engine: String,gameId: String,language: String,chapter: String) = offer(engine,gameId,language,chapter,"start","trial")
    override fun completedTrial(engine: String,gameId: String,language: String,chapter: String) = offer(engine,gameId,language,chapter,"complete","trial")
    private fun offer(engine: String,gameId: String,language: String,chapter: String,kind: String,mode: String="full",reason: String="") {
        if(!mutableEnabled.value) return
        val key=ChapterKey.validated(engine,gameId,language,chapter) ?: return
        operations.trySend(Operation.Event(key,kind,mode,reason))
    }
    private fun schedule() {
        if(scheduled) return
        runCatching {
            val request=PeriodicWorkRequestBuilder<StatisticsWorker>(1,TimeUnit.HOURS)
                .setInitialDelay(1,TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).setRequiresBatteryNotLow(true).build())
                .addTag(WORK_NAME).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME,ExistingPeriodicWorkPolicy.KEEP,request)
                .result.get(5,TimeUnit.SECONDS)
            scheduled=true
        }
    }
    private sealed interface Operation {
        data class Consent(val value: Boolean) : Operation
        data class Event(val key: ChapterKey,val kind: String,val mode: String,val reason: String) : Operation
    }
    private companion object { const val WORK_NAME="sutoko-chapter-statistics" }
}
