package purpletear.fr.purpleteartools

import org.junit.Assert.*
import org.junit.Test

class TableOfSymbolsLogicTest {

    @Test
    fun getReturnsNullForMissingSymbol() {
        val table = TableOfSymbols(1)
        assertNull(table.get(1, "missing"))
    }

    @Test
    fun addOrSetCreatesNewSymbol() {
        val table = TableOfSymbols(1)
        table.addOrSet(1, "key", "value")
        assertEquals("value", table.get(1, "key"))
    }

    @Test
    fun addOrSetOverwritesExistingValue() {
        val table = TableOfSymbols(1)
        table.addOrSet(1, "key", "first")
        table.addOrSet(1, "key", "second")
        assertEquals("second", table.get(1, "key"))
        assertEquals(1, table.getArray(1).size)
    }

    @Test
    fun hasSymbolReturnsFalseAfterDeleteRowData() {
        val table = TableOfSymbols(1)
        table.addOrSet(1, "key", "value")
        assertTrue(table.hasSymbol(1, "key"))
        table.deleteRowData(1)
        assertFalse(table.hasSymbol(1, "key"))
    }

    @Test
    fun resetPreservesStoryVersionAndReplayedFlag() {
        val table = TableOfSymbols(1)
        table.setStoryVersion(1, "1.2.3")
        table.addOrSet(1, "other", "data")
        table.reset(1)

        assertEquals("1.2.3", table.getStoryVersion(1))
        assertEquals("true", table.get(1, "replayedStory"))
        assertNull(table.get(1, "other"))
    }

    @Test
    fun chapterCodeDefaultsTo1a() {
        val table = TableOfSymbols(1)
        assertEquals("1a", table.chapterCode)
    }

    @Test
    fun chapterCodeReturnsLowercase() {
        val table = TableOfSymbols(1)
        table.chapterCode = "3B"
        assertEquals("3b", table.chapterCode)
    }

    @Test
    fun getAllRowIdsReflectsCurrentState() {
        val table = TableOfSymbols(1)
        table.addOrSet(1, "a", "1")
        table.addOrSet(2, "b", "2")
        assertEquals(setOf(1, 2), table.getAllRowIds())
        table.deleteRowData(1)
        assertEquals(setOf(2), table.getAllRowIds())
    }

    @Test
    fun conditionReturnsTrueForMatchingSymbol() {
        val table = TableOfSymbols(1)
        table.addOrSet(1, "key", "value")
        assertTrue(table.condition(1, "key", "value"))
    }

    @Test
    fun conditionReturnsFalseForNonMatchingValue() {
        val table = TableOfSymbols(1)
        table.addOrSet(1, "key", "value")
        assertFalse(table.condition(1, "key", "wrong"))
    }
}
