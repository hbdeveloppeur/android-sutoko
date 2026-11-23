package com.purpletear.sutoko.shop.data.repository

import com.purpletear.sutoko.shop.test.FakeShopApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ShopPackStoryCapacityTest {
    @Test
    fun `shop advertises the approved backend grants and complete story capacities`() = runTest {
        val packs = InMemoryShopRepository(FakeShopApi()).getPacks().getOrThrow()

        assertEquals(
            listOf("coins_pack_starter", "coins_pack_treasure", "coins_pack_mega"),
            packs.map { it.sku },
        )
        assertEquals(listOf(975, 2000, 4000), packs.map { it.coins })
        assertEquals(listOf(550, 1500, 3200), packs.map { it.diamonds })
        assertEquals(listOf(1, 2, 4), packs.map { it.coins / 975 })
        assertEquals(listOf(0, 50, 100), packs.map { it.coins % 975 })
    }
}
