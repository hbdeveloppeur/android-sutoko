package com.purpletear.game.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import purpletear.fr.purpleteartools.TableOfSymbols
import purpletear.fr.purpleteartools.symbols.SymbolsStorage

class FriendzonedProgressRepositoryImplTest {

    private class InMemorySymbolsStorage : SymbolsStorage {
        var stored: TableOfSymbols? = null

        override fun load(): TableOfSymbols? = stored

        override fun save(table: TableOfSymbols): Boolean {
            stored = table
            return true
        }
    }

    private val storage = InMemorySymbolsStorage()
    private val repository = FriendzonedProgressRepositoryImpl()

    @Before
    fun setUp() {
        TableOfSymbols.storage = storage
    }

    @After
    fun tearDown() {
        TableOfSymbols.storage = null
    }

    @Test
    fun `setFirstName persists the name in both the global and per-game rows`() = runTest {
        repository.setFirstName(legacyId = 162, name = "Alex")

        // The games read the store back through a fresh TableOfSymbols.
        val symbols = TableOfSymbols(162)
        symbols.read()
        assertEquals("Alex", symbols.globalFirstName)
        assertEquals("Alex", symbols.firstName)
    }

    @Test
    fun `setFirstName overwrites a previously stored name`() = runTest {
        repository.setFirstName(legacyId = 162, name = "Alex")
        repository.setFirstName(legacyId = 162, name = "Sam")

        val symbols = TableOfSymbols(162)
        symbols.read()
        assertEquals("Sam", symbols.globalFirstName)
        assertEquals("Sam", symbols.firstName)
    }

    @Test
    fun `setFirstName keeps the other games symbols untouched`() = runTest {
        val other = TableOfSymbols(159)
        other.read()
        other.chapterCode = "3b"
        other.save()

        repository.setFirstName(legacyId = 162, name = "Alex")

        val symbols = TableOfSymbols(159)
        symbols.read()
        assertEquals("3b", symbols.chapterCode)
        // Another game's per-game name stays on the default.
        assertEquals("Nick", symbols.firstName)
    }
}
