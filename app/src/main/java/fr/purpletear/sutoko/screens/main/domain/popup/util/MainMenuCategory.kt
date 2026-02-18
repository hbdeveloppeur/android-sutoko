package fr.purpletear.sutoko.screens.main.domain.popup.util

sealed class MainMenuCategory(val keywords: List<String>) {
    object All : MainMenuCategory(listOf("all"))
    object Free : MainMenuCategory(listOf("free"))
    object New : MainMenuCategory(listOf("new"))
}
