package fr.purpletear.sutoko.symbols

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import purpletear.fr.purpleteartools.TableOfSymbols
import purpletear.fr.purpleteartools.symbols.SymbolsStorage

class DefaultSymbolsRepositoryTest {

    @Test
    fun `load returns symbols from storage and caches them`() = runTest {
        val expected = TableOfSymbols(42).apply {
            setFirebaseNotification(false)
        }
        var loadCount = 0
        val fakeStorage = object : SymbolsStorage {
            override fun load(): TableOfSymbols? {
                loadCount++
                return expected
            }

            override fun save(table: TableOfSymbols): Boolean = true
        }

        val repository = DefaultSymbolsRepository { fakeStorage }

        val first = repository.load()
        val second = repository.load()

        assertEquals(expected.gameId, first.gameId)
        assertSame(first, second)
        assertEquals(1, loadCount)
        assertEquals(first, repository.symbols.value)
    }

    @Test
    fun `load returns empty table when storage returns null`() = runTest {
        val repository = DefaultSymbolsRepository {
            object : SymbolsStorage {
                override fun load(): TableOfSymbols? = null
                override fun save(table: TableOfSymbols): Boolean = false
            }
        }

        val result = repository.load()

        assertEquals(-1, result.gameId)
    }
}
