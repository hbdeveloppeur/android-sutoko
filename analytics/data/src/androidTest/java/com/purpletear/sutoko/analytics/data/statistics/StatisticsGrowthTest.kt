package com.purpletear.sutoko.analytics.data.statistics
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class StatisticsGrowthTest {
 private lateinit var store: StatisticsStore
 private lateinit var context: Context
 private val key=ChapterKey("sms","qa_story","en-us","1a")
 private val now=1800000000L
 @Before fun setup(){context=ApplicationProvider.getApplicationContext();context.deleteDatabase("growth-validation.db");store=StatisticsStore(context,"growth-validation.db");store.setEnabled(true)}
 @After fun cleanup(){store.close();context.deleteDatabase("growth-validation.db")}
 @Test fun previewIsLinkedToActualPlayingAndExplicitStoryEnding(){
  store.record(key.copy(chapter=""),"preview",now);store.record(key.copy(chapter=""),"play_click",now+1);store.record(key,"start",now+2);store.record(key,"complete",now+3);store.record(key,"story_complete",now+4)
  val events=store.reserveBatch(now+5);val preview=events.single{it.kind=="preview"};val session=events.single{it.kind=="session_start"}
  assertEquals(preview.eventId,session.previewId);assertEquals(preview.visitorId,session.visitorId);assertEquals(1,events.count{it.kind=="story_complete"});assertEquals(1,events.count{it.kind=="complete"})
 }
 @Test fun completedChapterDoesNotFinishStoryAndResumesDoNotBecomeReplays(){
  store.record(key,"start",now);store.record(key,"active",now+5);store.record(key,"complete",now+10)
  val events=store.reserveBatch(now+11);assertEquals(1,events.count{it.kind=="session_start"});assertFalse(events.any{it.kind=="story_complete"});assertFalse(events.any{it.replay})
 }
 @Test fun replayUsesNewRunButSameRecentStoryWindow(){
  store.record(key,"start",now);store.record(key,"complete",now+1);store.record(key,"story_complete",now+2);val first=store.reserveBatch(now+3);store.acknowledge(first)
  store.record(key.copy(chapter=""),"preview",now+5);store.record(key,"start",now+6);val replay=store.reserveBatch(now+3603)
  assertEquals(first[0].visitorId,replay[0].visitorId);assertNotEquals(first[0].runId,replay[0].runId);assertTrue(replay.any{it.kind=="story_start"&&it.replay})
 }
 @Test fun trialEndIsNotStoryCompletion(){
  store.record(key,"start",now,"trial");store.record(key,"complete",now+1,"trial");store.record(key,"trial_end",now+2,"trial");val events=store.reserveBatch(now+3)
  assertTrue(events.any{it.kind=="trial_end"});assertFalse(events.any{it.kind=="story_complete"})
 }
}
