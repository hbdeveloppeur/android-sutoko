package com.purpletear.game.data.repository.testing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreDeviceIdProviderTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var provider: DataStoreDeviceIdProvider

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope.backgroundScope
        ) {
            tempFolder.newFile("testing_prefs.preferences_pb")
        }
        provider = DataStoreDeviceIdProvider(dataStore)
    }

    @Test
    fun `get generates and persists a device id`() = testScope.runTest {
        val deviceId = provider.get()

        assertNotNull(deviceId)
        assertTrue(deviceId.isNotBlank())
    }

    @Test
    fun `get returns the same device id on subsequent calls`() = testScope.runTest {
        val first = provider.get()
        val second = provider.get()

        assertEquals(first, second)
    }

    @Test
    fun `get returns the stored device id after recreation`() = testScope.runTest {
        val firstProvider = DataStoreDeviceIdProvider(dataStore)
        val first = firstProvider.get()

        val secondProvider = DataStoreDeviceIdProvider(dataStore)
        val second = secondProvider.get()

        assertEquals(first, second)
    }
}
