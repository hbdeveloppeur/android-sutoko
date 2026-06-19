package purpletear.fr.purpleteartools

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TableOfSymbolsParcelableTest {

    @Test
    fun parcelRoundTripPreservesMapAndRoute() {
        val original = TableOfSymbols(161)
        original.addOrSet(161, "chapterCode", "7b")
        original.addOrSet(161, "prenom", "Alex")
        original.route[161] = arrayListOf("A", "B")

        val parcel = Parcel.obtain()
        original.writeToParcel(parcel, original.describeContents())
        parcel.setDataPosition(0)

        val restored = TableOfSymbols.CREATOR.createFromParcel(parcel)
        assertEquals("7b", restored.get(161, "chapterCode"))
        assertEquals("Alex", restored.get(161, "prenom"))
        assertEquals(listOf("A", "B"), restored.route[161])

        parcel.recycle()
    }

    @Test
    fun parcelRoundTripPreservesGameId() {
        val original = TableOfSymbols(42)
        val parcel = Parcel.obtain()
        original.writeToParcel(parcel, original.describeContents())
        parcel.setDataPosition(0)

        val restored = TableOfSymbols.CREATOR.createFromParcel(parcel)
        assertEquals(42, restored.gameId)

        parcel.recycle()
    }
}
