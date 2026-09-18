package com.purpletear.game.data.repository

import androidx.room.Room
import com.purpletear.game.data.database.GameDatabase
import com.purpletear.sutoko.game.model.chapter.MemoryEntry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class MemoryRepositoryHistoryTest {
    @Test
    fun replayRestoresTheValueFromThePreviousChapter() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(), GameDatabase::class.java,
        ).allowMainThreadQueries().build()
        try {
            val repository = MemoryRepositoryImpl(database.memoryDao())
            repository.save("story", mapOf("trust" to MemoryEntry("1", 1)))
            repository.save("story", mapOf("trust" to MemoryEntry("2", 2)))
            assertEquals(mapOf("trust" to MemoryEntry("1", 1)), repository.load("story", 2))
        } finally {
            database.close()
        }
    }

    @Test
    fun latestValuesAreObservedAndReplayOnlyDiscardsTheSelectedStoryFuture() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(), GameDatabase::class.java,
        ).allowMainThreadQueries().build()
        try {
            val repository = MemoryRepositoryImpl(database.memoryDao())
            repository.upsert("story", "trust", "1", 1)
            repository.upsert("story", "trust", "2", 2)
            repository.upsert("story", "trust", "3", 2)
            repository.upsert("story", "future", "yes", 3)
            repository.upsert("other", "trust", "9", 2)
            assertEquals(mapOf("trust" to "3", "future" to "yes"), repository.observe("story").first())
            assertEquals(mapOf("trust" to MemoryEntry("1", 1)), repository.load("story", 2))
            assertEquals(mapOf("trust" to "1"), repository.observe("story").first())
            assertEquals(mapOf("trust" to MemoryEntry("9", 2)), repository.load("other", 3))
            repository.upsert("story", "trust", "4", 2)
            assertEquals(mapOf("trust" to MemoryEntry("4", 2)), repository.load("story", 3))
            repository.clear("story")
            assertTrue(repository.load("story", 2).isEmpty())
            assertEquals(mapOf("trust" to "9"), repository.observe("other").first())
            repository.delete("other")
            assertTrue(repository.observe("other").first().isEmpty())
        } finally {
            database.close()
        }
    }

}
