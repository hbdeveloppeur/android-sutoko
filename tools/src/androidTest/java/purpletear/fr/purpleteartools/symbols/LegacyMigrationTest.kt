package purpletear.fr.purpleteartools.symbols

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import purpletear.fr.purpleteartools.symbols.data.SymbolEntity
import purpletear.fr.purpleteartools.symbols.data.SymbolsDatabase
import java.io.File

@RunWith(AndroidJUnit4::class)
class LegacyMigrationTest {

    private fun createStorage(legacyFile: File): SymbolsRoomStorage {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, SymbolsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        return SymbolsRoomStorage(db, legacyFile)
    }

    @Test
    fun migrateReadsLegacyJsonAndDeletesIt() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val legacyFile = File(context.cacheDir, "test_symbols.json")
        legacyFile.writeText(
            """{"gameId":-1,"map":{"0":[{"rowId":0,"n":"prenom","v":"Nick","identifier":-1}],"161":[{"rowId":161,"n":"chapterCode","v":"3a","identifier":-1}]},"route":{"161":["start","middle"]}}"""
        )

        val storage = createStorage(legacyFile)
        val loaded = storage.load()!!

        assertEquals("Nick", loaded.get(0, "prenom"))
        assertEquals("3a", loaded.get(161, "chapterCode"))
        assertEquals(listOf("start", "middle"), loaded.route[161])
        assertFalse(legacyFile.exists())
    }

    @Test
    fun migrateIsIdempotent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val legacyFile = File(context.cacheDir, "test_symbols_idempotent.json")
        legacyFile.writeText(
            """{"gameId":-1,"map":{"1":[{"rowId":1,"n":"k","v":"v","identifier":-1}]},"route":{}}"""
        )

        val storage = createStorage(legacyFile)
        storage.load()!!
        storage.load()!! // second call

        val loaded = storage.load()!!
        assertEquals(1, loaded.getArray(1).size)
        assertEquals("v", loaded.get(1, "k"))
    }

    @Test
    fun migrateSkipsWhenRoomAlreadyHasData() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val legacyFile = File(context.cacheDir, "test_symbols_skip.json")
        legacyFile.writeText(
            """{"gameId":-1,"map":{"1":[{"rowId":1,"n":"legacy","v":"old","identifier":-1}]},"route":{}}"""
        )

        val db = Room.inMemoryDatabaseBuilder(context, SymbolsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.symbolsDao()
        dao.insertOrUpdate(SymbolEntity(1, "existing", "current", -1))

        val storage = SymbolsRoomStorage(db, legacyFile)
        val loaded = storage.load()!!

        assertEquals("current", loaded.get(1, "existing"))
        assertNull(loaded.get(1, "legacy"))
        assertFalse(legacyFile.exists())
    }

    @Test
    fun migrateSurvivesCorruptedJson() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val legacyFile = File(context.cacheDir, "test_symbols_corrupt.json")
        legacyFile.writeText("this is not json")

        val storage = createStorage(legacyFile)
        val loaded = storage.load()

        assertNotNull(loaded)
        assertTrue(legacyFile.exists()) // preserved for retry
    }
}
