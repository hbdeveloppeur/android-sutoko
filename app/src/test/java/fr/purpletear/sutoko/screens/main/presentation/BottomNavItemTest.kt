package fr.purpletear.sutoko.screens.main.presentation

import fr.purpletear.sutoko.screens.main.presentation.screens.components.navigation.BottomNavItem
import org.junit.Assert.assertEquals
import org.junit.Test

class BottomNavItemTest {
    @Test
    fun playerNavigationContainsHomeCalendarCreateAndShop() {
        val items = listOf(BottomNavItem.Home, BottomNavItem.Calendar, BottomNavItem.Create, BottomNavItem.Shop)

        assertEquals(
            items.map { it.javaClass }.toSet(),
            BottomNavItem::class.java.declaredClasses.toSet(),
        )
        assertEquals(listOf("home", "calendar", "create", "shop"), items.map { it.route })
    }
}
