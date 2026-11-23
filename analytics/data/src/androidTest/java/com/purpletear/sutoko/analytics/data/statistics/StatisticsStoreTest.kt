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
class StatisticsStoreTest {
    private lateinit var store: StatisticsStore
    private lateinit var context: Context
    private val now=1800000000L
    private val key=ChapterKey("sms","story_1","fr-fr","1a")
    @Before fun setup() {
        context=ApplicationProvider.getApplicationContext(); context.deleteDatabase("statistics-validation.db")
        store=StatisticsStore(context,"statistics-validation.db"); store.setEnabled(true)
    }
    @After fun cleanup() { store.close(); context.deleteDatabase("statistics-validation.db") }
    @Test fun duplicateEntriesAndCompletionsDoNotMultiplyReports() {
        assertTrue(store.record(key,"start",now)); assertFalse(store.record(key,"start",now+1))
        assertTrue(store.record(key,"complete",now+2)); assertFalse(store.record(key,"complete",now+3))
        val batch=store.reserveBatch(now+4); assertEquals(4,batch.size); assertEquals(batch[0].runId,batch[1].runId)
        assertEquals("fr-fr",batch[0].key.language)
    }
    @Test fun unavailableEndpointCannotSpamOrDeletePendingReports() {
        store.record(key,"start",now); var calls=0
        StatisticsDelivery.attempt(store,now) { calls++; throw java.io.IOException("offline") }
        repeat(100) { StatisticsDelivery.attempt(store,now+1) { calls++; UploadOutcome.Accepted } }
        assertEquals(1,calls)
        StatisticsDelivery.attempt(store,now+3600) { calls++; UploadOutcome.Accepted }
        assertEquals(2,calls); assertTrue(store.reserveBatch(now+7200).isEmpty())
    }
    @Test fun optOutClearsQueueAndIdentifiers() {
        store.record(key,"start",now); store.setEnabled(false); assertFalse(store.isEnabled())
        store.setEnabled(true); assertTrue(store.reserveBatch(now).isEmpty())
        assertFalse(store.record(key,"complete",now+1))
    }
    @Test fun replaysAndTokenExpirationUseNewIdentifiers() {
        store.record(key,"start",now); store.record(key,"complete",now+1)
        val first=store.reserveBatch(now+2); store.acknowledge(first)
        store.record(key,"start",now+3); val replay=store.reserveBatch(now+3602)
        assertNotEquals(first[0].runId,replay[0].runId); assertTrue(replay[0].replay)
        store.acknowledge(replay)
        store.record(key,"start",now+DeliveryPolicy.RUN_TTL+3600)
        val rotated=store.reserveBatch(now+DeliveryPolicy.RUN_TTL+3600)
        assertNotEquals(replay[0].runId,rotated[0].runId)
    }
    @Test fun queueIsBoundedAndStaleReportsExpire() {
        repeat(700) { store.record(key.copy(gameId="story_$it"),"start",now+it) }
        val batch=store.reserveBatch(now+800); assertEquals(100,batch.size)
        store.acknowledge(batch)
        assertTrue(store.reserveBatch(now+DeliveryPolicy.QUEUE_TTL+1000).isEmpty())
    }
    @Test fun payloadContainsOnlyContractFields() {
        store.record(key,"start",now)
        val payload=org.json.JSONObject(StatisticsPayload.encode(store.reserveBatch(now)))
        val event=payload.getJSONArray("events").getJSONObject(0)
        assertEquals(setOf("event_id","run_id","visitor_id","preview_id","engine","game_id","language","chapter","kind","at","mode","reason","replay"),event.keys().asSequence().toSet())
    }
}
