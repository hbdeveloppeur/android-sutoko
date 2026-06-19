package purpletear.fr.purpleteartools

import org.junit.Assert.*
import org.junit.Test

class SymbolContractTest {

    @Test
    fun equalsIgnoresValue() {
        val a = Symbol(1, "name", "A")
        val b = Symbol(1, "name", "B")
        assertEquals(a, b)
    }

    @Test
    fun equalsReturnsFalseForDifferentRowId() {
        val a = Symbol(1, "name", "A")
        val b = Symbol(2, "name", "A")
        assertNotEquals(a, b)
    }

    @Test
    fun equalsReturnsFalseForDifferentName() {
        val a = Symbol(1, "foo", "A")
        val b = Symbol(1, "bar", "A")
        assertNotEquals(a, b)
    }

    @Test
    fun hashCodeIsStableWhenValueChanges() {
        val symbol = Symbol(1, "name", "A")
        val hashBefore = symbol.hashCode()
        symbol.v = "B"
        assertEquals(hashBefore, symbol.hashCode())
    }

    @Test
    fun hashCodeIsSameForEqualSymbols() {
        val a = Symbol(1, "name", "A")
        val b = Symbol(1, "name", "B")
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun hashCodeDistributionIsNotTrivial() {
        val a = Symbol(1, "foo", "x")
        val b = Symbol(1, "bar", "x")
        assertNotEquals(a.hashCode(), b.hashCode())
    }
}
