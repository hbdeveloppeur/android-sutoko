package purpletear.fr.purpleteartools.symbols

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import purpletear.fr.purpleteartools.TableOfSymbols
import purpletear.fr.purpleteartools.symbols.data.SymbolsDatabase
import java.io.File

@RunWith(AndroidJUnit4::class)
class SymbolsRoomStorageTest {

    private fun createStorage(): SymbolsRoomStorage {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, SymbolsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        return SymbolsRoomStorage(db, File(context.cacheDir, "dummy.json"))
    }

    @Test
    fun saveThenLoadRoundTrip() {
        val storage = createStorage()
        val original = TableOfSymbols(-1)
        original.addOrSet(0, "global", "yes")
        original.addOrSet(161, "chapterCode", "5a", 5)
        original.route[161] = arrayListOf("A", "B", "C")

        assertTrue(storage.save(original))

        val loaded = storage.load()
        assertNotNull(loaded)
        assertEquals("yes", loaded!!.get(0, "global"))
        assertEquals("5a", loaded.get(161, "chapterCode"))
        assertEquals(listOf("A", "B", "C"), loaded.route[161])
    }

    @Test
    fun loadReturnsEmptyTableWhenDatabaseIsEmpty() {
        val storage = createStorage()
        val loaded = storage.load()
        assertNotNull(loaded)
        assertTrue(loaded!!.getAllRowIds().isEmpty())
        assertTrue(loaded.route.isEmpty())
    }

    @Test
    fun saveOverwritesPreviousData() {
        val storage = createStorage()

        val first = TableOfSymbols(-1)
        first.addOrSet(1, "key", "first")
        storage.save(first)

        val second = TableOfSymbols(-1)
        second.addOrSet(2, "key", "second")
        storage.save(second)

        val loaded = storage.load()!!
        assertNull(loaded.get(1, "key"))
        assertEquals("second", loaded.get(2, "key"))
    }
}
