package fr.purpletear.sutoko.config

import android.os.Parcel
import com.example.sharedelements.SutokoAppParams
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Assert.assertEquals
import org.junit.Test

class SutokoAppParamsParcelCompatibilityTest {
    @Test
    fun removingAiAvailabilityPreservesTheLegacyParcelLayout() {
        val source = mockk<Parcel>()
        every { source.readByte() } returnsMany listOf<Byte>(1, 0, 1, 0, 1, 0)
        every { source.readInt() } returns 12
        every { source.readString() } returnsMany listOf(
            "instagram", "privacy", "report", "shop", "terms", "sku", "orders",
        )

        val params = SutokoAppParams.CREATOR.createFromParcel(source)
        assertEquals(12, params.leavingKeys)
        assertEquals("orders", params.myOrdersHeaderbackgroundUrl)
        verify(exactly = 6) { source.readByte() }
        verify(exactly = 7) { source.readString() }

        val destination = mockk<Parcel>(relaxed = true)
        params.writeToParcel(destination, 0)
        verifySequence {
            destination.writeByte(1)
            destination.writeInt(12)
            destination.writeByte(0)
            destination.writeByte(1)
            destination.writeByte(0)
            destination.writeByte(1)
            destination.writeString("instagram")
            destination.writeString("privacy")
            destination.writeString("report")
            destination.writeString("shop")
            destination.writeString("terms")
            destination.writeString("sku")
            destination.writeString("orders")
            destination.writeByte(1)
        }
    }
}
