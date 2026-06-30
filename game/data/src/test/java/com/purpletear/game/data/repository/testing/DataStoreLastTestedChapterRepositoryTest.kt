package com.purpletear.game.data.repository.testing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreLastTestedChapterRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: DataStoreLastTestedChapterRepository

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope.backgroundScope
        ) {
            tempFolder.newFile("testing_prefs.preferences_pb")
        }
        repository = DataStoreLastTestedChapterRepository(dataStore)
    }

    @Test
    fun `get returns null when no chapter was saved`() = testScope.runTest {
        assertNull(repository.get("story-1"))
    }

    @Test
    fun `set and get round-trip the chapter id`() = testScope.runTest {
        repository.set("story-1", "chapter-2")

        assertEquals("chapter-2", repository.get("story-1"))
    }

    @Test
    fun `values are keyed by story id`() = testScope.runTest {
        repository.set("story-1", "chapter-a")
        repository.set("story-2", "chapter-b")

        assertEquals("chapter-a", repository.get("story-1"))
        assertEquals("chapter-b", repository.get("story-2"))
    }

    @Test
    fun `set overwrites the previous value for the same story`() = testScope.runTest {
        repository.set("story-1", "chapter-1")
        repository.set("story-1", "chapter-2")

        assertEquals("chapter-2", repository.get("story-1"))
    }
}
