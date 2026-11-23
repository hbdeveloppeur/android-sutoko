package com.purpletear.sutoko.analytics.data.statistics

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StatisticsDefaultsTest {
    private lateinit var context: Context
    private val databaseName = "statistics-defaults-validation.db"
    private val now = 1800000000L
    private val key = ChapterKey("sms", "story_1", "fr-fr", "1a")

    @Before fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(databaseName)
    }

    @After fun cleanup() {
        context.deleteDatabase(databaseName)
    }

    @Test fun newStoreRecordsAndDeliversWithoutExplicitEnabling() {
        StatisticsStore(context, databaseName).use { store ->
            assertTrue(store.isEnabled())
            assertTrue(store.record(key, "start", now))
            assertTrue(store.reserveBatch(now).isNotEmpty())
        }
        StatisticsStore(context, databaseName).use { store ->
            assertTrue(store.isEnabled())
        }
    }

    @Test fun upgradePreservesDisabledStoreAndDeliveryBudget() {
        createVersionTwoStore(enabled = false)
        StatisticsStore(context, databaseName).use { store ->
            assertFalse(store.isEnabled())
            assertFalse(store.record(key, "start", now))
            assertTrue(store.reserveBatch(now).isEmpty())
            store.readableDatabase.rawQuery("SELECT failures, next_allowed FROM settings WHERE id=1", null).use {
                assertTrue(it.moveToFirst())
                assertEquals(2, it.getInt(0))
                assertEquals(now + DeliveryPolicy.HOUR, it.getLong(1))
            }
        }
        StatisticsStore(context, databaseName).use { store ->
            assertFalse(store.isEnabled())
        }
    }

    @Test fun upgradePreservesReportsForAlreadyEnabledStore() {
        createVersionTwoStore(enabled = true)
        StatisticsStore(context, databaseName).use { store ->
            assertTrue(store.isEnabled())
            assertEquals("preview", store.reserveBatch(now + DeliveryPolicy.HOUR).single().kind)
        }
    }

    private fun createVersionTwoStore(enabled: Boolean) {
        // Build the previous schema with its stored setting and an upload cooldown.
        StatisticsStore(context, databaseName).use { store ->
            store.setEnabled(enabled)
            if (enabled) assertTrue(store.record(key, "preview", now))
            store.writableDatabase.execSQL(
                "UPDATE settings SET next_allowed=?, failures=2 WHERE id=1",
                arrayOf(now + DeliveryPolicy.HOUR),
            )
            store.writableDatabase.version = 2
        }
    }
}
